package io.github.nyliummc.essentials.extension.teleport.api

import io.github.nyliummc.essentials.api.EssentialsProvider
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import net.minecraft.world.dimension.DimensionType

interface Teleportation {
    val destination: Vec3d
    val dimension: DimensionType
    val facing: Vec2f?

    interface Builder: io.github.nyliummc.essentials.api.util.Builder<Teleportation> {
        fun facing(facing: Vec2f)
        fun dimension(dimension: DimensionType)
        fun destination(destination: Vec3d)
    }

    companion object {
        fun builder(init: Builder.() -> Unit): Teleportation {
            val builder = EssentialsProvider.get().registry.supplyBuilder(Teleportation.Builder::class.java)
            builder.init()
            return builder.build()
        }
    }
}
