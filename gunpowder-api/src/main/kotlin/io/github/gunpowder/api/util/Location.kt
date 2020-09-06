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

/**
 * Represents a Location inside of a dimension with a rotation.
 */
data class Location(
        val position: Vec3d,
        val rotation: Vec2f?,
        val dimension: Identifier
) {
    /**
     * Gets the ServerWorld this Location is in
     */
    val world: ServerWorld?
        get() = GunpowderMod.instance.server.getWorld(RegistryKey.of(Registry.DIMENSION, dimension))
    
    companion object {
        @JvmStatic
        val ORIGIN = Location(Vec3d.ZERO, Vec2f.ZERO, World.OVERWORLD.value)

        @JvmStatic
        fun of(entity: Entity) = Location(entity.pos, Vec2f(entity.yaw, entity.pitch), entity.world.registryKey.value)
    }

    /**
     * Moves the position up by the specified amount.
     * @param distance the specified amount
     */
    fun up(distance: Int) = offset(distance, Direction.UP)
    fun up() = offset(Direction.UP)

    /**
     * Moves the position down by the specified amount.
     * @param distance the specified amount
     */
    fun down(distance: Int) = offset(distance, Direction.DOWN)
    fun down() = offset(Direction.DOWN)

    /**
     * Moves the position towards the north direction by the specified amount.
     * @param distance the specified amount
     */
    fun north(distance: Int) = offset(distance, Direction.NORTH)
    fun north() = offset(Direction.NORTH)

    /**
     * Moves the position towards the south direction by the specified amount.
     * @param distance the specified amount
     */
    fun south(distance: Int) = offset(distance, Direction.SOUTH)
    fun south() = offset(Direction.SOUTH)

    /**
     * Moves the position towards the west direction by the specified amount.
     * @param distance the specified amount
     */
    fun west(distance: Int) = offset(distance, Direction.WEST)
    fun west() = offset(Direction.WEST)

    /**
     * Moves the position towards the east direction by the specified amount.
     * @param distance the specified amount
     */
    fun east(distance: Int) = offset(distance, Direction.EAST)
    fun east() = offset(Direction.EAST)

    /**
     * Offsets the position in the specified direction by one block.
     * @param direction the specified direction
     */
    fun offset(direction: Direction) = offset(1, direction)

    /**
     * Offsets the position in the specified direction by the specified amount.
     * @param amount the specified amount
     * @param direction the specified direction
     */
    fun offset(amount: Int, direction: Direction) = if (amount == 0) this else Location(
            Vec3d(
                    position.x + direction.offsetX * amount,
                    position.y + direction.offsetY * amount,
                    position.z + direction.offsetZ * amount
            ), rotation, dimension
    )

    /**
     * Rotates the position align the axis by the specified rotation.
     * @param rotation the specified block rotation
     */
    fun axisAlignedRotate(rotation: BlockRotation) = when (rotation) {
        BlockRotation.CLOCKWISE_90 -> {
            Location(Vec3d(-position.z, position.y, position.x), this.rotation, dimension)
        }
        BlockRotation.CLOCKWISE_180 -> {
            Location(Vec3d(-position.x, position.y, -position.z), this.rotation, dimension)
        }
        BlockRotation.COUNTERCLOCKWISE_90 -> {
            Location(Vec3d(position.z, position.y, -position.x), this.rotation, dimension)
        }
        else -> this
    }

    /**
     * Returns a new Location with the rotation provided and the position and dimension of this Location.
     * @param yaw the Yaw rotation
     * @param pitch the Pitch rotation
     */
    fun withRotation(yaw: Float, pitch: Float) = withRotation(Vec2f(yaw, pitch))

    /**
     * Returns a new Location with the rotation provided and the position and dimension of this Location.
     * @param vector the Yaw and Pitch rotations
     */
    fun withRotation(vector: Vec2f) = Location(position, vector, dimension)

    /**
     * Returns a new Location with the dimension provided and the position and rotation of this Location.
     * @param world the world to get the dimension from
     */
    fun withDimension(world: World) = withDimension(world.registryKey.value)

    /**
     * Returns a new Location with the dimension provided and the position and rotation of this Location.
     * @param dimension the Identifier of the dimension
     */
    fun withDimension(dimension: Identifier) = Location(position, rotation, dimension)

    /**
     * Returns a new Location with the dimension provided and the position and rotation of this Location.
     * @param key the registry key of the dimension
     */
    fun withPosition(key: RegistryKey<World>) = withDimension(key.value)

    /**
     * Returns a new Location with the dimension provided and the position and rotation of this Location.
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param z the Z coordinate
     */
    fun withPosition(x: Double, y: Double, z: Double) = withPosition(Vec3d(x, y, z))

    /**
     * Returns a new Location with the dimension provided and the position and rotation of this Location.
     * @param vector The position to set
     */
    fun withPosition(vector: Vec3d) = Location(vector, rotation, dimension)
    fun withPosition(vector: Vec3i) = withPosition(
            Vec3d(vector.x.toDouble(), vector.y.toDouble(), vector.z.toDouble())
    )
}
