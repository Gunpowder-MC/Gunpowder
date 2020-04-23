package io.github.nyliummc.essentials.entities.builders

import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import net.minecraft.world.dimension.DimensionType
import java.time.temporal.TemporalUnit
import io.github.nyliummc.essentials.api.builders.TeleportRequest as ITeleportRequest

class TeleportRequest(override val destination: Vec3d,
                      override val dimension: DimensionType,
                      override val facing: Vec2f?) : ITeleportRequest {

    override fun execute(time: Long, unit: TemporalUnit) {
        // TODO
    }

    class Builder : ITeleportRequest.Builder {
        private var destination: Vec3d? = null
        private var dimension: DimensionType? = null
        private var facing: Vec2f? = null

        override fun facing(facing: Vec2f) {
            this.facing = facing
        }

        override fun dimension(dimension: DimensionType) {
            this.dimension = dimension
        }

        override fun destination(destination: Vec3d) {
            this.destination = destination
        }

        override fun build(): TeleportRequest {
            return TeleportRequest(destination!!, dimension!!, facing)
        }
    }
}
