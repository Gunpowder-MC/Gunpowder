package io.github.nyliummc.essentials.modelhandlers

import io.github.nyliummc.essentials.api.EssentialsMod
import io.github.nyliummc.essentials.api.module.mcmmo.dataholders.StoredMcMMOSkill
import io.github.nyliummc.essentials.models.UserSkillTable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.*
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.roundToInt
import io.github.nyliummc.essentials.api.module.mcmmo.modelhandlers.McMMOSkillRegistry as APISkillRegistry

object McMMOSkillRegistry : APISkillRegistry {
    private val db by lazy {
        EssentialsMod.instance.database
    }
    private val skillCache = mutableMapOf<UUID, MutableMap<APISkillRegistry.SkillKey, StoredMcMMOSkill>>()
    private val callbackMap = mutableMapOf<APISkillRegistry.SkillKey, MutableList<(StoredMcMMOSkill) -> Unit>>()

    init {
        loadAllSkills()
    }

    private fun loadAllSkills() {
        transaction {
            UserSkillTable.selectAll().forEach { row ->
                val skillName = row[UserSkillTable.skill]
                val key = getKey(skillName)
                skillCache.getOrPut(row[UserSkillTable.user]) { mutableMapOf() }[key] =
                        StoredMcMMOSkill(row[UserSkillTable.user], key, row[UserSkillTable.exp], row[UserSkillTable.level])
            }
        }
    }

    class SkillKey(val name: String) : APISkillRegistry.SkillKey {
        override fun getUser(user: UUID): StoredMcMMOSkill {
            return skillCache.getOrPut(user) { mutableMapOf() }.getOrPut(this) {
                db.transaction {
                    UserSkillTable.insert {
                        it[UserSkillTable.user] = user
                        it[UserSkillTable.skill] = this@SkillKey.name
                        it[UserSkillTable.exp] = 0
                        it[UserSkillTable.level] = 0
                    }
                }
                StoredMcMMOSkill(user, this, 0, 0)
            }
        }

        override fun addUserExp(user: UUID, amount: Int) {
            val u = getUser(user)
            u.skillExp += amount
            checkExp(u)
            db.transaction {
                UserSkillTable.update({ UserSkillTable.user.eq(user).and(UserSkillTable.skill.eq(this@SkillKey.name)) }) {
                    it[UserSkillTable.exp] = u.skillExp
                    it[UserSkillTable.level] = u.skillLevel
                }
            }
        }

        override fun setUserExp(user: UUID, amount: Int) {
            val u = getUser(user)
            u.skillExp = amount
            checkExp(u)
            db.transaction {
                UserSkillTable.update({ UserSkillTable.user.eq(user).and(UserSkillTable.skill.eq(this@SkillKey.name)) }) {
                    it[UserSkillTable.exp] = u.skillExp
                    it[UserSkillTable.level] = u.skillLevel
                }
            }
        }

        override fun setUserLevel(user: UUID, level: Int) {
            val lv = if (level > 100) 100 else level
            val u = getUser(user)
            u.skillLevel = lv
            db.transaction {
                UserSkillTable.update({ UserSkillTable.user.eq(user).and(UserSkillTable.skill.eq(this@SkillKey.name)) }) {
                    it[UserSkillTable.level] = u.skillLevel
                }
            }
        }

        private fun checkExp(user: StoredMcMMOSkill) {
            if (user.skillLevel >= 100) return

            val exp = user.skillExp
            val expNeeded = getExpNeeded(user.skillLevel)
            if (exp >= expNeeded) {
                user.skillExp = exp - expNeeded
                user.skillLevel += 1
                callbackMap[this]!!.forEach {
                    it.invoke(user)
                }
            }
        }
    }

    override fun registerSkill(skillName: String): APISkillRegistry.SkillKey {
        return callbackMap.keys.firstOrNull { (it as SkillKey).name == skillName } ?: run {
            val key = SkillKey(skillName)
            callbackMap[key] = mutableListOf()
            key
        }
    }

    override fun registerSkillUpCallback(skill: APISkillRegistry.SkillKey, callback: (StoredMcMMOSkill) -> Unit) {
        callbackMap.getOrPut(skill) { mutableListOf() }.add(callback)
    }

    override fun getKey(skillName: String): APISkillRegistry.SkillKey {
        return callbackMap.keys.first { (it as SkillKey).name == skillName }
    }

    private fun getExpNeeded(currentLevel: Int): Int {
        // Formula: Pokemon [Fluctuating]

        val n = (currentLevel + 1).toDouble()
        return when {
            n <= 15 -> {
                (n.pow(3.0) * ((floor((n + 1) / 3) + 24) / 50))
            }
            n <= 36 -> {
                (n.pow(3.0) * ((n + 14) / 50))
            }
            else -> {
                (n.pow(3.0) * ((floor(n / 2) + 32) / 50))
            }
        }.roundToInt()
    }
}
