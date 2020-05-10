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
import net.minecraft.world.World
import net.minecraft.world.dimension.DimensionType
import org.dynmap.DynmapChunk
import org.dynmap.DynmapLocation
import org.dynmap.DynmapWorld
import org.dynmap.utils.MapChunkCache
import org.dynmap.utils.Polygon


class FabricDynmapWorld(var world: World) : DynmapWorld(getName(world.dimension.type), world.height, world.seaLevel) {
    override fun isNether(): Boolean {
        return world.dimension.isNether
    }

    override fun getSpawnLocation(): DynmapLocation {
        val spawn = world.spawnPos
        return DynmapLocation(name, spawn.x.toDouble(), spawn.y.toDouble(), spawn.z.toDouble())
    }

    override fun getTime(): Long {
        return world.time
    }

    override fun hasStorm(): Boolean {
        return world.isRaining
    }

    override fun isThundering(): Boolean {
        return world.isThundering
    }

    override fun isLoaded(): Boolean {
        // TODO
        return true
    }

    override fun setWorldUnloaded() {
        // TODO
    }

    override fun getLightLevel(x: Int, y: Int, z: Int): Int {
        return world.getLightLevel(LightType.BLOCK, BlockPos(x, y, z))
    }

    override fun getHighestBlockYAt(x: Int, z: Int): Int {
        return world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z)
    }

    override fun canGetSkyLightLevel(): Boolean {
        return world.dimension.hasSkyLight()
    }

    override fun getSkyLightLevel(x: Int, y: Int, z: Int): Int {
        return world.getLightLevel(LightType.SKY, BlockPos(x, y, z))
    }

    private val envMap = mapOf(
            "world" to "normal",
            "world_nether" to "nether",
            "world_end" to "the_end"
    )

    override fun getEnvironment(): String {
        return envMap.getOrDefault(name, "normal")
    }

    override fun getChunkCache(chunks: List<DynmapChunk>): MapChunkCache {
        return FabricDynmapMapChunkCache(this, world as ServerWorld, chunks) // TODO

    }

    override fun getWorldBorder(): Polygon? {
        val wb = world.worldBorder
        if (wb != null && wb.size < 5.9E7) {
            val p = Polygon()
            p.addVertex(wb.boundWest, wb.boundNorth)
            p.addVertex(wb.boundWest, wb.boundSouth)
            p.addVertex(wb.boundEast, wb.boundSouth)
            p.addVertex(wb.boundEast, wb.boundNorth)
            return p
        }
        return null
    }

    companion object {
        private fun getName(type: DimensionType): String {
            return "world" + type.suffix
        }
    }
}
