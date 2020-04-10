package io.github.nyliummc.essentials.impl.user

import com.mojang.authlib.GameProfile
import io.github.nyliummc.essentials.api.user.User
import net.minecraft.server.network.ServerPlayerEntity

class PlayerBackedUser(playerEntity: ServerPlayerEntity) : User {
    override val profile: GameProfile = playerEntity.gameProfile

    override val isOnline: Boolean = true
}