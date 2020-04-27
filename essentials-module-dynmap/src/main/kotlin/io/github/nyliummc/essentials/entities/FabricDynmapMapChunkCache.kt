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