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

package io.github.nyliummc.essentials.entities

import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.Heightmap
import net.minecraft.world.LightType
import org.dynmap.DynmapChunk
import org.dynmap.DynmapLocation
import org.dynmap.DynmapWorld
import org.dynmap.utils.MapChunkCache


internal class FabricDynmapWorld(private var world: ServerWorld?) : DynmapWorld(world!!.dimension.type.suffix, world.effectiveHeight, world.seaLevel) {
    override fun isNether(): Boolean {
        return world!!.dimension.isNether
    }

    override fun getSpawnLocation(): DynmapLocation {
        val spawn = world!!.spawnPos
        return DynmapLocation(this.rawName, spawn.x.toDouble(), spawn.y.toDouble(), spawn.z.toDouble())
    }

    override fun getTime(): Long {
        return world!!.time
    }

    override fun hasStorm(): Boolean {
        return world!!.isRaining
    }

    override fun isThundering(): Boolean {
        return world!!.isThundering
    }

    override fun isLoaded(): Boolean {
        return world != null
    }

    override fun setWorldUnloaded() {
        world = null
    }

    override fun getLightLevel(x: Int, y: Int, z: Int): Int {
        return world!!.getLightLevel(LightType.BLOCK, BlockPos(x, y, z))
    }

    override fun getHighestBlockYAt(x: Int, z: Int): Int {
        return world!!.getTopY(Heightmap.Type.WORLD_SURFACE, x, z)
    }

    override fun canGetSkyLightLevel(): Boolean {
        return world!!.dimension.hasSkyLight()
    }

    override fun getSkyLightLevel(x: Int, y: Int, z: Int): Int {
        return world!!.getLightLevel(LightType.SKY, BlockPos(x, y, z))
    }

    override fun getEnvironment(): String {
        return world!!.getDimension().type.suffix
    }

    override fun getChunkCache(chunks: List<DynmapChunk>): MapChunkCache {
        return FabricDynmapMapChunkCache(this, chunks)
    }
}
