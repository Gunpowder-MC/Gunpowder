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

package io.github.gunpowder.api.builders

import io.github.gunpowder.api.GunpowderMod
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import net.minecraft.util.registry.RegistryKey
import net.minecraft.world.World
import net.minecraft.world.dimension.DimensionType
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit
import java.util.function.Consumer

interface TeleportRequest {
    val player: ServerPlayerEntity
    val destination: Vec3d
    val dimension: Identifier
    val facing: Vec2f?
    val callback: (() -> Unit)?

    companion object {
        @JvmStatic
        fun builder(callback: Consumer<Builder>) = builder(callback::accept)
        fun builder(callback: Builder.() -> Unit): TeleportRequest {
            val builder = GunpowderMod.instance.registry.getBuilder(Builder::class.java)
            callback(builder)
            return builder.build()
        }
    }

    /**
     * Time (in seconds) after which to execute the teleport.
     */
    fun execute(seconds: Long) {
        execute(seconds, ChronoUnit.SECONDS)
    }

    /**
     * Time after which to execute the teleport.
     */
    fun execute(time: Long, unit: TemporalUnit)

    interface Builder {
        /**
         * Player to teleport.
         */
        fun player(player: ServerPlayerEntity)

        /**
         * Direction the player faces (not required).
         */
        fun facing(facing: Vec2f)

        /**
         * Dimension to teleport to (not required)
         */
        fun dimension(dimension: Identifier)
        fun dimension(dimension: RegistryKey<World>) = dimension(dimension.value)
        fun dimension(dimension: World) = dimension(dimension.registryKey)

        /**
         * Coordinates to teleport to.
         */
        fun destination(destination: Vec3d)
        fun destination(destination: Vec3i) {
            destination(Vec3d.of(destination))
        }

        /**
         * Callback to run after teleporting (not required).
         */
        fun onComplete(callback: Runnable) = onComplete(callback::run)
        fun onComplete(callback: () -> Unit)

        @Deprecated("Used internally, do not use.")
        fun build(): TeleportRequest
    }
}
