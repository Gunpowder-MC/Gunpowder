package io.github.nyliummc.essentials.modelhandlers

import io.github.nyliummc.essentials.api.modules.chat.dataholders.StoredNickname
import io.github.nyliummc.essentials.api.modules.chat.modelhandlers.NicknameHandler as APINicknameHandler
import io.github.nyliummc.essentials.models.NicknameTable
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.*

object NicknameHandler : APINicknameHandler {
    private val cache: MutableMap<UUID, StoredNickname> = mutableMapOf()

    override fun getUser(user: UUID): StoredNickname {
        return cache[user] ?: transaction {
            val userObj = NicknameTable.select { NicknameTable.user.eq(user) }.first()
            val StoredNickname = StoredNickname(
                    user,
                    userObj[NicknameTable.nickname])
            cache[user] = StoredNickname
            StoredNickname
        }
    }

    override fun updateUser(user: StoredNickname) {
        cache[user.uuid] = user
        transaction {
            NicknameTable.update({
                NicknameTable.user.eq(user.uuid)
            }) {
                it[nickname] = user.nickname
            }
        }
    }

    override fun modifyUser(user: UUID, callable: (StoredNickname) -> StoredNickname) {
        updateUser(callable(getUser(user)))
    }
}