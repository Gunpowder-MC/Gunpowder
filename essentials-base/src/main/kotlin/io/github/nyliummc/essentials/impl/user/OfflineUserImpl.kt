package io.github.nyliummc.essentials.impl.user

import com.mojang.authlib.GameProfile
import io.github.nyliummc.essentials.api.user.User

class OfflineUserImpl(override val profile: GameProfile) : User {

    override val isOnline: Boolean = false
}