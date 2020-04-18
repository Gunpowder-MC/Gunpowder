package io.github.nyliummc.essentials.impl.user

import com.mojang.authlib.GameProfile
import io.github.nyliummc.essentials.api.user.User
import net.minecraft.util.Identifier

class OfflineUser(override val profile: GameProfile) : User {
    override val isOnline: Boolean = false
    override fun has(permission: Identifier): Boolean {
        return false
    }
}