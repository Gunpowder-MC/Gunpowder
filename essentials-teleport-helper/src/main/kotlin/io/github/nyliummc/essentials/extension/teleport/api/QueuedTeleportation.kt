package io.github.nyliummc.essentials.extension.teleport.api

import io.github.nyliummc.essentials.api.user.User
import java.time.Instant
import java.util.*

interface QueuedTeleportation {
    val user: User
    val id: UUID
    val teleportation: Teleportation
    val submitted: Instant
    val apply: Instant
    fun cancel()
}