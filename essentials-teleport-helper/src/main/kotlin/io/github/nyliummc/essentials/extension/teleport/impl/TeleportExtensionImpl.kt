package io.github.nyliummc.essentials.extension.teleport.impl

import io.github.nyliummc.essentials.api.user.User
import io.github.nyliummc.essentials.base.extension.AbstractEssentialsExtension
import io.github.nyliummc.essentials.impl.EssentialsImpl
import io.github.nyliummc.essentials.impl.user.ServerUser
import io.github.nyliummc.essentials.extension.teleport.api.QueuedTeleportation
import io.github.nyliummc.essentials.extension.teleport.api.TeleportExtension
import io.github.nyliummc.essentials.extension.teleport.api.Teleportation
import net.minecraft.server.network.ServerPlayerEntity
import java.time.Instant
import java.time.temporal.TemporalUnit
import java.util.*
import kotlin.collections.HashMap

class TeleportExtensionImpl(essentials: EssentialsImpl) : AbstractEssentialsExtension("teleport", essentials), TeleportExtension {
    // We do not store user instances, instead query the user manager with a UUID
    private val teleportQueue: MutableMap<UUID, QueuedTeleportationImpl> = HashMap()

    override fun awaitingTeleportation(user: User): Boolean {
        return teleportQueue.contains(user.profile.id)
    }

    override fun getTeleportation(user: User): Optional<QueuedTeleportation> {
        return Optional.ofNullable(teleportQueue[user.profile.id])
    }

    override fun teleportNow(user: User, teleportation: Teleportation) {
        if (user is ServerUser) {
            val player: ServerPlayerEntity = user.playerEntity
            val pitch: Float = teleportation.facing?.x ?: player.pitch
            val yaw: Float = teleportation.facing?.y ?: player.yaw
            player.teleport(player.server.getWorld(teleportation.dimension)!!, teleportation.destination.getX(), teleportation.destination.getY(), teleportation.destination.getZ(), yaw, pitch)
        }
    }

    override fun enqueue(user: User, teleportation: Teleportation, applyUnit: TemporalUnit, applyLength: Long): QueuedTeleportation {
        val now: Instant = Instant.now()
        val applyInstant: Instant = Instant.now().plus(applyLength, applyUnit)
        return QueuedTeleportationImpl(user, UUID.randomUUID(), teleportation, now, applyInstant, this)
    }

    fun tick() {
        // Remove queued entries which have been cancelled now
        for (entry in teleportQueue) {
            if (entry.value.isCancelled()) {
                teleportQueue.remove(entry.key)
                continue
            }

            // Now fire teleportations which have queued long enough
            if (Instant.now() >= entry.value.apply) { // Either we are at the exact instant or past it is when we should execute
                teleportNow(entry.value.user, entry.value.teleportation)
                // Remove the entry after we fire off the teleportation
                teleportQueue.remove(entry.key)
            }
        }
    }
}