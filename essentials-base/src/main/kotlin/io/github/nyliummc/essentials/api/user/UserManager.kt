package io.github.nyliummc.essentials.api.user

import net.minecraft.server.network.ServerPlayerEntity
import java.util.*
import java.util.concurrent.CompletableFuture

interface UserManager {
    fun getOnline(username: String): Optional<User>
    fun getOnline(uuid: UUID): Optional<User>
    fun getOnline(playerEntity: ServerPlayerEntity): User
    operator fun get(uuid: UUID): Optional<CompletableFuture<User>>
    operator fun get(username: String): Optional<CompletableFuture<User>>
}