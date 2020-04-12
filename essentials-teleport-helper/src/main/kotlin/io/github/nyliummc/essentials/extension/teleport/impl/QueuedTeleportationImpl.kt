package io.github.nyliummc.essentials.extension.teleport.impl

import io.github.nyliummc.essentials.api.user.User
import io.github.nyliummc.essentials.extension.teleport.api.QueuedTeleportation
import io.github.nyliummc.essentials.extension.teleport.api.Teleportation
import java.time.Instant
import java.util.*

class QueuedTeleportationImpl(
        override val user: User,
        override val id: UUID,
        override val teleportation: Teleportation,
        override val submitted: Instant,
        override val apply: Instant,
        extensionImpl: TeleportExtensionImpl) : QueuedTeleportation {
    private var cancelled: Boolean = false

    fun isCancelled(): Boolean {
        return cancelled
    }

    override fun cancel() {
        cancelled = true
    }
}