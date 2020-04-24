/*
 * MIT License
 *
 * Copyright (c) NyliumMC
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
