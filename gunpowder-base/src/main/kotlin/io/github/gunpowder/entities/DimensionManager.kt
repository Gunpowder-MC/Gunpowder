/*
 * MIT License
 *
 * Copyright (c) GunpowderMC
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

package io.github.gunpowder.entities

import com.google.common.collect.ImmutableList
import io.github.gunpowder.api.GunpowderMod
import io.github.gunpowder.mixin.cast.SyncPlayer
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.registry.RegistryKey
import net.minecraft.world.World
import net.minecraft.world.border.WorldBorder
import net.minecraft.world.border.WorldBorderListener
import net.minecraft.world.dimension.DimensionType
import net.minecraft.world.gen.chunk.ChunkGenerator
import net.minecraft.world.level.UnmodifiableLevelProperties


object DimensionManager {
    val server: MinecraftServer
        get() = GunpowderMod.instance.server

    fun addDimensionType(dimensionTypeId: RegistryKey<DimensionType>, dimensionType: DimensionType) {
        server.dimensionTracker.addDimensionType(dimensionTypeId, dimensionType)
    }

    fun addWorld(worldId: RegistryKey<World>, dimensionTypeId: RegistryKey<DimensionType>, chunkGenerator: ChunkGenerator): ServerWorld {
        val generatorOptions = server.saveProperties.generatorOptions
        val dimensionType = server.dimensionTracker.registry.get(dimensionTypeId)!!
        val serverWorldProperties = server.saveProperties.mainWorldProperties

        val overworld = server.worlds[World.OVERWORLD]!!
        val worldGenerationProgressListener = overworld.chunkManager.threadedAnvilChunkStorage.worldGenerationProgressListener
        val seed = generatorOptions.seed
        val worldBorder = if (dimensionType.isShrunk) {
            object : WorldBorder() {
                override fun getCenterX(): Double {
                    return super.getCenterX() / 8.0
                }

                override fun getCenterZ(): Double {
                    return super.getCenterZ() / 8.0
                }
            }
        } else {
            WorldBorder()
        }

        val props = UnmodifiableLevelProperties(server.saveProperties, serverWorldProperties)
        val world = ServerWorld(server, server.workerExecutor, server.session,
                                props, worldId, dimensionTypeId, dimensionType,
                                worldGenerationProgressListener, chunkGenerator,
                                false, seed, ImmutableList.of(), false)
        worldBorder.addListener(WorldBorderListener.WorldBorderSyncer(world.worldBorder))

        server.worlds[worldId] = world

        for (player in server.playerManager.playerList) {
            (player as SyncPlayer).setNeedsSync(true)
        }

        return world
    }
}
