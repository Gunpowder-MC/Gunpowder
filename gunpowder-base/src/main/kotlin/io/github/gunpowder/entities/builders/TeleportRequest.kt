/*
 * MIT License
 *
 * Copyright (c) GunpowderMC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.gunpowder.entities.builders

import io.github.gunpowder.events.PlayerPreTeleportCallback
import io.github.gunpowder.events.PlayerTeleportCallback
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ChunkTicketType
import net.minecraft.util.ActionResult
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryKey
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.TemporalUnit
import kotlin.concurrent.thread
import io.github.gunpowder.api.builders.TeleportRequest as APITeleportRequest

class TeleportRequest private constructor(
        override val player: ServerPlayerEntity,
        override val destination: Vec3d,
        override val dimension: Identifier,
        override val facing: Vec2f?,
        override val callback: (() -> Unit)?
) : APITeleportRequest {

    override fun execute(time: Long, unit: TemporalUnit) {
        val now = LocalDateTime.now()
        val server = player.server

        thread(start = true, isDaemon = true) {
            val duration = Duration.between(LocalDateTime.now(), now.plus(time, unit)).toMillis()
            if (duration > 0) {
                Thread.sleep(duration)
            }

            // Verify they're still online
            if (server.playerManager.playerList.contains(player)) {
                val res = PlayerPreTeleportCallback.EVENT.invoker().trigger(player, this)
                if (res != ActionResult.FAIL) {
                    PlayerTeleportCallback.EVENT.invoker().trigger(player, this)
                    server.submit {
                        // Load chunk
                        val chunkPos = ChunkPos(BlockPos(destination))
                        val world = server.getWorld(RegistryKey.of(Registry.DIMENSION, dimension))

                        // field_19347 = POST_TELEPORT
                        world!!.chunkManager.addTicket(ChunkTicketType.POST_TELEPORT, chunkPos, 1, player.entityId)

                        player.teleport(world,
                                destination.x, destination.y, destination.z,
                                facing?.x ?: player.yaw, facing?.y ?: player.pitch)

                        callback?.invoke()
                    }
                }
            }
        }
    }

    class Builder : APITeleportRequest.Builder {
        private var destination: Vec3d? = null
        private var dimension: Identifier? = null
        private var facing: Vec2f? = null
        private var player: ServerPlayerEntity? = null
        private var callback: (() -> Unit)? = null

        override fun player(player: ServerPlayerEntity) {
            this.player = player
            if (dimension == null) {
                dimension = player.world.registryKey.value
            }
        }

        override fun facing(facing: Vec2f) {
            this.facing = facing
        }

        override fun dimension(dimension: Identifier) {
            this.dimension = dimension
        }

        override fun destination(destination: Vec3d) {
            this.destination = destination
        }

        override fun onComplete(callback: () -> Unit) {
            this.callback = callback
        }

        override fun build(): TeleportRequest {
            return TeleportRequest(player!!, destination!!, dimension!!, facing, callback)
        }
    }
}
