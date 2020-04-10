package io.github.nyliummc.essentials.impl.user

import io.github.nyliummc.essentials.api.user.User
import io.github.nyliummc.essentials.api.user.UserManager
import io.github.nyliummc.essentials.impl.AbstractEssentialsMod
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import java.time.Instant
import java.util.*
import java.util.concurrent.CompletableFuture

class UserManagerImpl : UserManager {
    /**
     * @apiNote values obtained by this map are dirty and should not be cached. Instead you should query the UserManager every time you need the user.
     */
    private val onlineUsers: Map<UUID, PlayerBackedUser> = IdentityHashMap()
    /**
     * @apiNote values obtained by this map are dirty and should not be cached. Values here are removed soon in the future.
     * TODO Implement this
     */
    private val loadedOfflineUsers: Map<UUID, OfflineUserEntry> = IdentityHashMap()

    override fun getOnline(username: String): Optional<User> { // We have to query the server to get a user from the username.
        val playerEntity = server()!!.playerManager.getPlayer(username)
        return if (playerEntity != null) {
            Optional.ofNullable(onlineUsers[playerEntity.uuid])
        } else Optional.empty()
    }

    override fun getOnline(uuid: UUID): Optional<User> {
        return Optional.ofNullable(onlineUsers[uuid])
    }

    override fun getOnline(playerEntity: ServerPlayerEntity): User {
        return this.getOnline(playerEntity.uuid).get()
    }

    override fun get(uuid: UUID): Optional<CompletableFuture<User>> {
        val user = this.getOnline(uuid)
        return if (user.isPresent) {
            user.map { u -> CompletableFuture.completedFuture(u)}
        } else Optional.empty()
        // TODO Implement offline checks
    }

    override fun get(username: String): Optional<CompletableFuture<User>> {
        val user = this.getOnline(username)
        return if (user.isPresent) {
            user.map { u -> CompletableFuture.completedFuture(u)}
        } else Optional.empty()
        // TODO Implement offline checks
    }

    fun server(): MinecraftServer? {
        return AbstractEssentialsMod.currentServer
    }

    private class OfflineUserEntry private constructor(private val user: OfflineUserImpl) {
        private val added: Instant = Instant.now()
    }
}