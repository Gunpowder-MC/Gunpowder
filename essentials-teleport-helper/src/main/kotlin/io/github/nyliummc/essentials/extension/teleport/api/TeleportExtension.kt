package io.github.nyliummc.essentials.extension.teleport.api

import io.github.nyliummc.essentials.api.extension.EssentialsExtension
import io.github.nyliummc.essentials.api.user.User
import java.time.temporal.TemporalUnit
import java.util.*

interface TeleportExtension: EssentialsExtension {
    fun awaitingTeleportation(user: User): Boolean

    fun getTeleportation(user: User): Optional<QueuedTeleportation>

    fun teleportNow(user: User, teleportation: Teleportation)

    fun enqueue(user: User, teleportation: Teleportation, applyUnit: TemporalUnit, applyLength: Long): QueuedTeleportation
}