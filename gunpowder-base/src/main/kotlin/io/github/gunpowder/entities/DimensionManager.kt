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
import io.github.gunpowder.api.GunpowderDimensionManager
import io.github.gunpowder.api.GunpowderMod
import io.github.gunpowder.api.builders.TeleportRequest
import io.github.gunpowder.mixin.cast.SyncPlayer
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.registry.RegistryKey
import net.minecraft.world.World
import net.minecraft.world.border.WorldBorder
import net.minecraft.world.border.WorldBorderListener
import net.minecraft.world.dimension.DimensionType
import net.minecraft.world.gen.chunk.ChunkGenerator
import net.minecraft.world.level.ServerWorldProperties
import net.minecraft.world.level.UnmodifiableLevelProperties
import org.apache.commons.io.FileUtils


object DimensionManager : GunpowderDimensionManager {
    val server: MinecraftServer
        get() = GunpowderMod.instance.server

    override fun hasDimensionType(dimensionTypeId: RegistryKey<DimensionType>): Boolean {
        return server.dimensionTracker.registry.containsId(dimensionTypeId.value)
    }

    override fun addDimensionType(dimensionTypeId: RegistryKey<DimensionType>, dimensionType: DimensionType) {
        if (hasDimensionType(dimensionTypeId)) {
            throw IllegalArgumentException("DimensionType ${dimensionTypeId.value} already registered!")
        }

        server.dimensionTracker.addDimensionType(dimensionTypeId, dimensionType)
    }

    override fun removeDimensionType(dimensionTypeId: RegistryKey<DimensionType>) {
        if (dimensionTypeId == DimensionType.OVERWORLD_REGISTRY_KEY || dimensionTypeId == DimensionType.THE_NETHER_REGISTRY_KEY || dimensionTypeId == DimensionType.THE_END_REGISTRY_KEY) {
            return  // don't remove default
        }

        if (!hasDimensionType(dimensionTypeId)) {
            return
        }

        val dtype = server.dimensionTracker.registry.entriesById[dimensionTypeId.value]
        server.dimensionTracker.registry.entriesById.remove(dimensionTypeId.value)
        server.dimensionTracker.registry.entriesByKey.remove(dimensionTypeId)
        val i = server.dimensionTracker.registry.indexedEntries.getId(dtype)
        server.dimensionTracker.registry.indexedEntries.put(null, i)
    }

    override fun hasWorld(worldId: RegistryKey<World>): Boolean {
        return server.worlds.containsKey(worldId)
    }

    override fun addWorld(worldId: RegistryKey<World>, dimensionTypeId: RegistryKey<DimensionType>, chunkGenerator: ChunkGenerator, properties: ServerWorldProperties): ServerWorld {
        if (hasWorld(worldId)) {
            throw IllegalArgumentException("World ${worldId.value} already registered!")
        }

        val generatorOptions = server.saveProperties.generatorOptions
        val dimensionType = server.dimensionTracker.registry.get(dimensionTypeId)!!

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

        val props = UnmodifiableLevelProperties(server.saveProperties, properties)
        val world = ServerWorld(server, server.workerExecutor, server.session,
                                props, worldId, dimensionTypeId, dimensionType,
                                worldGenerationProgressListener, chunkGenerator,
                                false, seed, ImmutableList.of(), false)
        worldBorder.addListener(WorldBorderListener.WorldBorderSyncer(world.worldBorder))

        server.worlds[worldId] = world

        for (player in server.playerManager.playerList) {
            GunpowderMod.instance.logger.info("Marking needsSync for player $player")
            (player as SyncPlayer).setNeedsSync(true)
        }

        return world
    }

    override fun removeWorld(worldId: RegistryKey<World>) {
        if (worldId == World.END || worldId == World.NETHER || worldId == World.OVERWORLD) {
            return  // Not deleting default worlds
        }

        if (!hasWorld(worldId)) {
            return  // No such world
        }

        val world = server.worlds.remove(worldId)
        if (world != null) {

            val path = world.server.session.getWorldDirectory(world.registryKey)
            FileUtils.deleteDirectory(path)

            for (player in server.playerManager.playerList) {
                if (player.spawnPointDimension == worldId) {
                    player.setSpawnPoint(null, null, false, false)
                }

                GunpowderMod.instance.logger.info("Marking needsSync for player $player")
                (player as SyncPlayer).setNeedsSync(true)

                // Teleport players in the dimension to be removed
                if (player.world == world) {
                    TeleportRequest.builder {
                        val sw = server.getWorlds().first { it.registryKey == player.spawnPointDimension }
                        dimension(sw)
                        destination(player.spawnPointPosition ?: sw.spawnPos)
                        player(player)
                    }.execute(0)
                }
            }
        }
    }
}
