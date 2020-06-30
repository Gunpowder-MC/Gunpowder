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

import com.mojang.datafixers.util.Either
import io.github.nyliummc.essentials.mixin.dynmap.ServerChunkManagerAccessor_Dynmap
import net.minecraft.server.world.ChunkHolder.Unloaded
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.registry.Registry
import net.minecraft.world.LightType
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.chunk.ChunkStatus
import org.dynmap.DynmapChunk
import org.dynmap.DynmapWorld
import org.dynmap.common.BiomeMap
import org.dynmap.hdmap.HDBlockModels
import org.dynmap.renderer.DynmapBlockState
import org.dynmap.renderer.RenderPatchFactory
import org.dynmap.utils.BlockStep
import org.dynmap.utils.MapChunkCache
import org.dynmap.utils.MapIterator
import org.dynmap.utils.VisibilityLimit
import java.util.*
import java.util.concurrent.CompletableFuture


class FabricDynmapMapChunkCache(private val fworld: FabricDynmapWorld,
                                private val world: ServerWorld, chunks: List<DynmapChunk>): MapChunkCache() {
    private val chunkManager = world.chunkManager
    private val chunksToLoad = LinkedList<ChunkPos>()
    private val chunkFutures = arrayListOf<CompletableFuture<Either<Chunk, Unloaded>>>()
    private val chunks = hashMapOf<ChunkPos, Chunk>()
    private var requiredStatus = ChunkStatus.FEATURES

    init {
        for (c in chunks) {
            chunksToLoad.add(ChunkPos(c.x, c.z))
        }
    }

    override fun setChunkDataTypes(blockdata: Boolean, biome: Boolean, highestblocky: Boolean, rawbiome: Boolean): Boolean {
        requiredStatus = ChunkStatus.FEATURES

        if (highestblocky) {
            requiredStatus = ChunkStatus.FULL
        }

        return true
    }

    override fun loadChunks(maxToLoad: Int): Int {
        val maxToLoadNum = chunksToLoad.size.coerceAtMost(maxToLoad)

        for (i in 0 until maxToLoadNum) {
            val pos = chunksToLoad.remove()
            chunkManager as ServerChunkManagerAccessor_Dynmap
            chunkFutures.add(chunkManager.invokeGetChunkFuture(pos.x, pos.z, requiredStatus, false))
        }

        return chunks.size + maxToLoadNum
    }

    override fun isDoneLoading(): Boolean {
        val futureIterator = chunkFutures.iterator()
        while (futureIterator.hasNext()) {
            val future = futureIterator.next()
            if (future.isDone) {
                futureIterator.remove()
                try {
                    future.get().ifLeft { c ->
                        println("Fetched chunk: $c")
                        chunks[c.pos] = c
                    }
                } catch (e: Exception) {
                    throw RuntimeException(e)
                }
            }
        }

        println("isDoneLoading(): ${chunkFutures.isEmpty()}")
        return chunkFutures.isEmpty()
    }

    override fun isEmpty(): Boolean {
        return chunks.isEmpty();
    }

    override fun unloadChunks() {
        chunkFutures.clear();
        chunks.clear();
    }
    override fun isEmptySection(sx: Int, sy: Int, sz: Int): Boolean {
        val c = chunks[ChunkPos(sx, sz)]
        return c?.sectionArray?.get(sy)?.isEmpty ?: false
    }

    override fun getIterator(x: Int, y: Int, z: Int): MapIterator? {
        return FabricDynmapMapIterator()
    }

    inner class FabricDynmapMapIterator : MapIterator {
        private var lastStep: BlockStep? = null
        private var pos: BlockPos.Mutable? = null
        override fun initialize(x0: Int, y0: Int, z0: Int) {
            pos = BlockPos.Mutable(x0, y0, z0)
        }

        override fun getBlockSkyLight(): Int {
            return world.getLightLevel(LightType.SKY, pos)
        }

        override fun getBlockEmittedLight(): Int {
            return world.getLightLevel(LightType.BLOCK, pos)
        }

        override fun getBiome(): BiomeMap {
            return BiomeMap.byBiomeID(Registry.BIOME.getRawId(world.getBiome(pos)))
        }

        override fun getSmoothGrassColorMultiplier(colormap: IntArray): Int {
            return world.getBiome(pos).getGrassColorAt(pos!!.x.toDouble(), pos!!.z.toDouble()) // TODO
        }

        override fun getSmoothFoliageColorMultiplier(colormap: IntArray): Int {
            return world.getBiome(pos).foliageColor // TODO
        }

        override fun getSmoothWaterColorMultiplier(): Int {
            return world.getBiome(pos).waterColor // TODO
        }

        override fun getSmoothWaterColorMultiplier(colormap: IntArray): Int {
            return world.getBiome(pos).waterColor // TODO
        }

        override fun getSmoothColorMultiplier(colormap: IntArray, swampcolormap: IntArray): Int {
            return world.getBiome(pos).getGrassColorAt(pos!!.x.toDouble(), pos!!.z.toDouble()) // TODO
        }

        override fun stepPosition(step: BlockStep) {
            lastStep = step
            pos = pos!!.add(step.xoff, step.yoff, step.zoff) as BlockPos.Mutable?
        }

        override fun unstepPosition(step: BlockStep) {
            stepPosition(unstep[step.ordinal])
        }

        override fun unstepPosition(): BlockStep {
            val ls = lastStep
            stepPosition(unstep[lastStep!!.ordinal])
            return ls!!
        }

        override fun setY(y: Int) {
            if (y == pos!!.y) return
            lastStep = if (y > pos!!.y) BlockStep.Y_PLUS else BlockStep.Y_MINUS
            pos!!.y = y
        }

        override fun getBlockTypeAt(s: BlockStep): DynmapBlockState? {
            val poso = pos!!.add(s.xoff, s.yoff, s.zoff)
            return FabricDynmapBlockStateMapper.INSTANCE[world.getBlockState(poso)]
        }

        override fun getLastStep(): BlockStep {
            return lastStep!!
        }

        override fun getWorldHeight(): Int {
            return fworld.worldheight
        }

        override fun getBlockKey(): Long {
            return pos!!.asLong()
        }

        override fun isEmptySection(): Boolean {
            return world.getChunk(pos).sectionArray[pos!!.y shr 4].isEmpty
        }

        override fun getInhabitedTicks(): Long {
            return world.getChunk(pos).inhabitedTime
        }

        override fun getPatchFactory(): RenderPatchFactory? {
            return HDBlockModels.getPatchDefinitionFactory()
        }

        override fun getBlockType(): DynmapBlockState? {
            return FabricDynmapBlockStateMapper.INSTANCE[world.getBlockState(pos)]
        }

        override fun getBlockTileEntityField(fieldId: String): Any? {
            val entity = world.getBlockEntity(pos)
            return entity?.javaClass?.getDeclaredField(fieldId)?.get(entity)
        }

        override fun getBlockTypeAt(xoff: Int, yoff: Int, zoff: Int): DynmapBlockState? {
            val poso = pos!!.add(xoff, yoff, zoff)
            return FabricDynmapBlockStateMapper.INSTANCE[world.getBlockState(poso)]
        }

        override fun getBlockTileEntityFieldAt(fieldId: String, xoff: Int, yoff: Int, zoff: Int): Any? {
            val poso = pos!!.add(xoff, yoff, zoff)
            val entity = world.getBlockEntity(poso)
            return entity?.javaClass?.getDeclaredField(fieldId)?.get(entity)
        }

        override fun getX(): Int {
            return pos!!.x
        }

        override fun getY(): Int {
            return pos!!.y
        }

        override fun getZ(): Int {
            return pos!!.z
        }
    }

    override fun setHiddenFillStyle(style: HiddenChunkStyle) {
        // TODO("Not yet implemented")
    }

    override fun setVisibleRange(limit: VisibilityLimit) {
        // TODO("Not yet implemented")
    }

    override fun setHiddenRange(limit: VisibilityLimit) {
        // TODO("Not yet implemented")
    }

    override fun getWorld(): DynmapWorld? {
        return fworld
    }

    companion object {
        private val unstep = arrayOf(BlockStep.X_MINUS, BlockStep.Y_MINUS, BlockStep.Z_MINUS,
                BlockStep.X_PLUS, BlockStep.Y_PLUS, BlockStep.Z_PLUS
        )
    }
}
