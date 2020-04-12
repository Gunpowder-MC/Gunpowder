package io.github.nyliummc.essentials.extension.teleport.impl

import io.github.nyliummc.essentials.extension.teleport.api.Teleportation
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import net.minecraft.world.dimension.DimensionType

class TeleportationBuilderImpl: Teleportation.Builder {
    var facing: Vec2f? = null
    var dimension: DimensionType? = null
    var destination: Vec3d? = null

    override fun facing(facing: Vec2f) {
        this.facing = facing
    }

    override fun dimension(dimension: DimensionType) {
        this.dimension = dimension
    }

    override fun destination(destination: Vec3d) {
        this.destination = destination
    }

    override fun build(): Teleportation {
        return TeleportationImpl(destination!!, dimension!!, facing)
    }
}