package io.github.nyliummc.essentials.api.module.mcmmo.modelhandlers

import io.github.nyliummc.essentials.api.module.mcmmo.dataholders.StoredMcMMOSkill
import java.util.*

interface McMMOSkillRegistry {
    interface SkillKey {
        fun getUser(user: UUID): StoredMcMMOSkill
        fun addUserExp(user: UUID, amount: Int)
        fun setUserExp(user: UUID, amount: Int)
        fun setUserLevel(user: UUID, level: Int)
    }

    fun registerSkill(skillName: String): SkillKey
    fun registerSkillUpCallback(skill: SkillKey, callback: (StoredMcMMOSkill)->Unit)

    fun getKey(skillName: String): SkillKey
}
