package io.github.nyliummc.essentials.api.modules.chat.modelhandlers

import io.github.nyliummc.essentials.api.modules.chat.dataholders.StoredNickname
import java.util.*

interface NicknameHandler {
    fun getUser(user: UUID): StoredNickname
    fun updateUser(user: StoredNickname)
    fun modifyUser(user: UUID, callable: (StoredNickname) -> StoredNickname)
}
