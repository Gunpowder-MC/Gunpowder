package io.github.nyliummc.essentials.api.user

import com.mojang.authlib.GameProfile

// TODO: Flesh out specifics of the user data
interface User {
    val profile: GameProfile
    val isOnline: Boolean
}