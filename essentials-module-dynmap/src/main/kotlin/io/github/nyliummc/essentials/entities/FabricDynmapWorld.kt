package io.github.nyliummc.essentials.entities

import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.Heightmap
import net.minecraft.world.LightType
import org.dynmap.DynmapChunk
import org.dynmap.DynmapLocation
import org.dynmap.DynmapWorld
import org.dynmap.utils.MapChunkCache


internal class FabricDynmapWorld(private val world: ServerWorld) : DynmapWorld(world.dimension.type.suffix, world.effectiveHeight, world.seaLevel) {
    override fun isNether(): Boolean {
        return world.dimension.isNether
    }

    override fun getSpawnLocation(): DynmapLocation {
        val spawn = world.spawnPos
        return DynmapLocation(this.rawName, spawn.x.toDouble(), spawn.y.toDouble(), spawn.z.toDouble())
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
        TODO("Not yet implemented")
    }

    override fun setWorldUnloaded() {
        TODO("Not yet implemented")
    }

    override fun getLightLevel(x: Int, y: Int, z: Int): Int {
        return world.getLightLevel(LightType.BLOCK, BlockPos(x, y, z))
    }

    override fun getHighestBlockYAt(x: Int, z: Int): Int {
        return world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z)
    }

    override fun canGetSkyLightLevel(): Boolean {
        // Always return true for now; It'll return 0 if unable
        return true
    }

    override fun getSkyLightLevel(x: Int, y: Int, z: Int): Int {
        return world.getLightLevel(LightType.SKY, BlockPos(x, y, z))
    }

    override fun getEnvironment(): String {
        return world.getDimension().type.suffix
    }

    override fun getChunkCache(chunks: List<DynmapChunk>): MapChunkCache {
        return FabricDynmapMapChunkCache(this, chunks)
    }
}
