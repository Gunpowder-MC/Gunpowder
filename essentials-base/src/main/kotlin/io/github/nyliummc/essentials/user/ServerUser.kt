package io.github.nyliummc.essentials.impl.user

import com.mojang.authlib.GameProfile
import io.github.nyliummc.essentials.api.user.User
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier

class ServerUser(var playerEntity: ServerPlayerEntity) : User {
    override val profile: GameProfile = playerEntity.gameProfile
    override val isOnline: Boolean = true
    override fun has(permission: Identifier): Boolean {
        TODO("not implemented")
    }
}