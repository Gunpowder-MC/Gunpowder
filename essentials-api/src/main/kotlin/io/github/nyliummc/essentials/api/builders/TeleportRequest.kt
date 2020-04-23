package io.github.nyliummc.essentials.api.builders

import io.github.nyliummc.essentials.api.EssentialsMod
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import net.minecraft.world.dimension.DimensionType
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit

interface TeleportRequest {
    val destination: Vec3d
    val dimension: DimensionType
    val facing: Vec2f?

    companion object {
        @JvmStatic
        fun builder(callback: Builder.() -> Unit): TeleportRequest {
            val builder = EssentialsMod.instance!!.registry.getBuilder(Builder::class.java)
            callback(builder)
            return builder.build()
        }
    }

    fun execute(seconds: Long) {
        execute(seconds, ChronoUnit.SECONDS)
    }

    fun execute(time: Long, unit: TemporalUnit)

    interface Builder {
        fun facing(facing: Vec2f)
        fun dimension(dimension: DimensionType)
        fun destination(destination: Vec3d)

        @Deprecated("Used internally, do not use.")
        fun build(): TeleportRequest
    }
}
