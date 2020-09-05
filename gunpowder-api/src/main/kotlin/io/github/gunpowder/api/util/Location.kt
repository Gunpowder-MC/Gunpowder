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

package io.github.gunpowder.api.util

import io.github.gunpowder.api.GunpowderMod
import net.minecraft.entity.Entity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.BlockRotation
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryKey
import net.minecraft.world.World

data class Location(
        val position: Vec3d,
        val rotation: Vec2f?,
        val dimension: Identifier
) {
    val world: ServerWorld?
        get() = GunpowderMod.instance.server.getWorld(RegistryKey.of(Registry.DIMENSION, dimension))

    companion object {
        @JvmStatic
        val ORIGIN = Location(Vec3d.ZERO, Vec2f.ZERO, World.OVERWORLD.value)

        @JvmStatic
        fun of(entity: Entity) = Location(entity.pos, Vec2f(entity.yaw, entity.pitch), entity.world.registryKey.value)
    }

    fun up() = offset(Direction.UP)
    fun up(distance: Int) = offset(distance, Direction.UP)

    fun down() = offset(Direction.DOWN)
    fun down(distance: Int) = offset(distance, Direction.DOWN)

    fun north() = offset(Direction.NORTH)
    fun north(distance: Int) = offset(distance, Direction.NORTH)

    fun south() = offset(Direction.SOUTH)
    fun south(distance: Int) = offset(distance, Direction.SOUTH)

    fun west() = offset(Direction.WEST)
    fun west(distance: Int) = offset(distance, Direction.WEST)

    fun east() = offset(Direction.EAST)
    fun east(distance: Int) = offset(distance, Direction.EAST)

    fun offset(direction: Direction): Location = Location(
            Vec3d(
                    position.x + direction.offsetX, position.y + direction.offsetY, position.z + direction.offsetZ
            ), rotation, dimension
    )

    fun offset(amount: Int, direction: Direction): Location = if (amount == 0) this else Location(
            Vec3d(
                    position.x + direction.offsetX * amount,
                    position.y + direction.offsetY * amount,
                    position.z + direction.offsetZ * amount
            ), rotation, dimension
    )

    fun axisAlignedRotate(rot: BlockRotation): Location {
        return when (rot) {
            BlockRotation.CLOCKWISE_90 -> {
                Location(Vec3d(-position.z, -position.y, -position.x), rotation, dimension)
            }
            BlockRotation.CLOCKWISE_180 -> {
                Location(Vec3d(-position.x, -position.y, -position.z), rotation, dimension)
            }
            BlockRotation.COUNTERCLOCKWISE_90 -> {
                Location(Vec3d(position.z, position.y, -position.x), rotation, dimension)
            }
            else -> this
        }
    }

    fun withRotation(yaw: Float, pitch: Float) = withRotation(Vec2f(yaw, pitch))
    fun withRotation(vector: Vec2f) = Location(position, vector, dimension)

    fun withDimension(world: World) = withDimension(world.registryKey.value)
    fun withDimension(dimension: Identifier) = Location(position, rotation, dimension)

    fun withPosition(x: Double, y: Double, z: Double) = withPosition(Vec3d(x, y, z))
    fun withPosition(position: Vec3i) = withPosition(Vec3d(position.x.toDouble(), position.y.toDouble(), position.z.toDouble()))
    fun withPosition(position: Vec3d) = Location(position, rotation, dimension)
}
