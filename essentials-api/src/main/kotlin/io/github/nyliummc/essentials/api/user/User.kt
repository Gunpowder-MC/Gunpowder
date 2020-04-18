package io.github.nyliummc.essentials.api.user

import com.mojang.authlib.GameProfile
import net.minecraft.util.Identifier

// TODO: Flesh out specifics of the user data
interface User {
    val profile: GameProfile
    val isOnline: Boolean

    /**
     * Checks if this user has a permission.
     *
     * @return true if this user has the permission.
     * False if the user is offline or does not have the permission.
     */
    fun has(permission: Identifier): Boolean
}