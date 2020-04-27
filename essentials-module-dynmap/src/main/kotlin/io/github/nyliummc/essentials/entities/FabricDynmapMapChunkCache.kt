package io.github.nyliummc.essentials.entities

import org.dynmap.DynmapChunk
import org.dynmap.DynmapWorld
import org.dynmap.utils.MapChunkCache
import org.dynmap.utils.MapIterator
import org.dynmap.utils.VisibilityLimit


class FabricDynmapMapChunkCache(
        private val world: DynmapWorld,
        private val chunks: List<DynmapChunk?>?) : MapChunkCache() {
    override fun setChunkDataTypes(blockdata: Boolean, biome: Boolean, highestblocky: Boolean, rawbiome: Boolean): Boolean {
        TODO("Not yet implemented")
    }

    override fun loadChunks(maxToLoad: Int): Int {
        TODO("Not yet implemented")
    }

    override fun isDoneLoading(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isEmpty(): Boolean {
        TODO("Not yet implemented")
    }

    override fun unloadChunks() {
        TODO("Not yet implemented")
    }
    override fun isEmptySection(sx: Int, sy: Int, sz: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun getIterator(x: Int, y: Int, z: Int): MapIterator? {
        TODO("Not yet implemented")
    }

    override fun setHiddenFillStyle(style: HiddenChunkStyle) {
        TODO("Not yet implemented")
    }

    override fun setVisibleRange(limit: VisibilityLimit) {
        TODO("Not yet implemented")
    }

    override fun setHiddenRange(limit: VisibilityLimit) {
        TODO("Not yet implemented")
    }

    override fun getWorld(): DynmapWorld? {
        return world
    }
}
