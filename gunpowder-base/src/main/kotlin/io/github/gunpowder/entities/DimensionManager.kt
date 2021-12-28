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

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.google.common.collect.ImmutableBiMap
import com.mojang.serialization.Dynamic
import com.mojang.serialization.DynamicOps
import io.github.gunpowder.api.GunpowderDimensionManager
import io.github.gunpowder.api.GunpowderMod
import io.github.gunpowder.api.builders.TeleportRequest
import io.github.gunpowder.cast.SyncPlayer
import net.minecraft.SharedConstants
import net.minecraft.datafixer.DataFixTypes
import net.minecraft.datafixer.Schemas
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.NbtOps
import net.minecraft.resource.DataPackSettings
import net.minecraft.resource.ResourceManager
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.dynamic.RegistryOps
import net.minecraft.util.registry.DynamicRegistryManager
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryKey
import net.minecraft.util.registry.SimpleRegistry
import net.minecraft.world.World
import net.minecraft.world.border.WorldBorder
import net.minecraft.world.border.WorldBorderListener
import net.minecraft.world.dimension.DimensionType
import net.minecraft.world.gen.Spawner
import net.minecraft.world.gen.chunk.ChunkGenerator
import net.minecraft.world.level.LevelInfo
import net.minecraft.world.level.LevelProperties
import net.minecraft.world.level.storage.LevelStorage
import net.minecraft.world.level.storage.SaveVersionInfo
import org.apache.commons.io.FileUtils
import java.io.File


object DimensionManager : GunpowderDimensionManager {
    val server: MinecraftServer
        get() = GunpowderMod.instance.server
    private val dimTypeRegistry: SimpleRegistry<DimensionType>
        get() {
            return server.registryManager[Registry.DIMENSION_TYPE_KEY] as SimpleRegistry<DimensionType>
        }

    val netherMap = HashBiMap.create<RegistryKey<World>, RegistryKey<World>>()
    private val linkedWorldSet = mutableSetOf<RegistryKey<World>>(World.OVERWORLD, World.NETHER, World.END)
    private val custom = mutableSetOf<RegistryKey<World>>()

    init {
        netherMap[World.OVERWORLD] = World.NETHER
    }

    override fun hasDimensionType(dimensionTypeId: RegistryKey<DimensionType>): Boolean {
        return dimTypeRegistry.keyToEntry.containsKey(dimensionTypeId)
    }

    override fun addDimensionType(dimensionTypeId: RegistryKey<DimensionType>, dimensionType: DimensionType) {
        if (hasDimensionType(dimensionTypeId)) {
            throw IllegalArgumentException("DimensionType ${dimensionTypeId.value} already registered!")
        }

        Registry.register(dimTypeRegistry, dimensionTypeId.value, dimensionType)
    }

    override fun removeDimensionType(dimensionTypeId: RegistryKey<DimensionType>) {
        if (dimensionTypeId == DimensionType.OVERWORLD_REGISTRY_KEY || dimensionTypeId == DimensionType.THE_NETHER_REGISTRY_KEY || dimensionTypeId == DimensionType.THE_END_REGISTRY_KEY) {
            return  // don't remove default
        }

        if (!hasDimensionType(dimensionTypeId)) {
            return
        }

        val dtype = dimTypeRegistry.idToEntry[dimensionTypeId.value]
        dimTypeRegistry.idToEntry.remove(dimensionTypeId.value)
        dimTypeRegistry.keyToEntry.remove(dimensionTypeId)
        dimTypeRegistry.entryToLifecycle.remove(dtype)
        dimTypeRegistry.rawIdToEntry.remove(dtype)
        dimTypeRegistry.entryToRawId.removeInt(dtype)
    }

    override fun hasWorld(worldId: RegistryKey<World>): Boolean {
        return server.worlds.containsKey(worldId)
    }

    fun isCustom(world: RegistryKey<World>) : Boolean {
        return custom.contains(world)
    }

    private fun getProps(key: RegistryKey<World>) : LevelProperties? {
        val target = File(server.session.getWorldDirectory(key).toFile(), "level.dat")
        if (target.exists()) {
            val tag = NbtIo.readCompressed(target)
            val dataFixer = Schemas.getFixer()
            val compoundTag3 = if (tag.contains("Player", 10)) tag.getCompound("Player") else null
            tag.remove("Player")
            val i = if (tag.contains("DataVersion", 99)) tag.getInt("DataVersion") else -1
            val dynamicOps: DynamicOps<NbtElement> =
                RegistryOps.of(NbtOps.INSTANCE, ResourceManager.Empty.INSTANCE, DynamicRegistryManager.create())
            val dynamic = dataFixer.update(
                DataFixTypes.LEVEL.typeReference,
                Dynamic(dynamicOps, tag),
                i,
                SharedConstants.getGameVersion().worldVersion
            )
            val pair = LevelStorage.readGeneratorProperties(dynamic, dataFixer, i)
            val saveVersionInfo = SaveVersionInfo.fromDynamic(dynamic)
            val levelInfo = LevelInfo.fromDynamic(dynamic, DataPackSettings.SAFE_MODE)
            return LevelProperties.readProperties(
                dynamic,
                dataFixer,
                i,
                compoundTag3,
                levelInfo,
                saveVersionInfo,
                pair.first,
                pair.second
            )
        }
        return null
    }

    override fun addWorld(worldId: RegistryKey<World>, dimensionTypeId: RegistryKey<DimensionType>, chunkGenerator: ChunkGenerator, properties: LevelProperties, spawners: List<Spawner>): ServerWorld {
        if (hasWorld(worldId)) {
            throw IllegalArgumentException("World ${worldId.value} already registered!")
        }

        val dimensionType = dimTypeRegistry.get(dimensionTypeId)!!

        val overworld = server.worlds[World.OVERWORLD]!!
        val worldGenerationProgressListener = overworld.chunkManager.threadedAnvilChunkStorage.worldGenerationProgressListener
        val seed = chunkGenerator.worldSeed
        val worldBorder = object : WorldBorder() {
            override fun getCenterX(): Double {
                return super.getCenterX() / dimensionType.coordinateScale
            }

            override fun getCenterZ(): Double {
                return super.getCenterZ() / dimensionType.coordinateScale
            }
        }

        val targetDir = server.session.getWorldDirectory(worldId).toFile()
        targetDir.mkdirs()

        val props = getProps(worldId) ?: properties

        val world = ServerWorld(server, server.workerExecutor, server.session,
                props, worldId, dimensionType,
                worldGenerationProgressListener, chunkGenerator,
                false, seed, spawners, !dimTypeRegistry.get(dimensionTypeId)!!.hasFixedTime())
        world.savingDisabled = false
        worldBorder.addListener(WorldBorderListener.WorldBorderSyncer(world.worldBorder))

        server.worlds[worldId] = world
        custom.add(worldId)

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

            val path = world.server.session.getWorldDirectory(world.registryKey).toFile()
            FileUtils.deleteDirectory(path)

            for (player in server.playerManager.playerList) {
                if (player.spawnPointDimension.toString() == worldId.toString()) {
                    player.setSpawnPoint(null, null, 0.0f, false, false)
                }

                GunpowderMod.instance.logger.info("Marking needsSync for player $player")
                (player as SyncPlayer).setNeedsSync(true)

                // Teleport players in the dimension to be removed
                if (player.world == world) {
                    TeleportRequest.builder {
                        val sw = server.getWorlds().first {
                            it.registryKey.toString() == player.spawnPointDimension.toString()
                        }
                        dimension(sw)
                        destination(player.spawnPointPosition ?: sw.spawnPos)
                        player(player)
                    }.execute(0)
                }
            }
        }
    }

    override fun linkNether(overworld: RegistryKey<World>, nether: RegistryKey<World>) {
        if (overworld in linkedWorldSet || nether in linkedWorldSet) {
            throw IllegalArgumentException("World already linked!")
        }
        linkedWorldSet.addAll(listOf(overworld, nether))
        netherMap[overworld] = nether
    }

    override fun getLinkedWorlds(): BiMap<RegistryKey<World>, RegistryKey<World>> {
        return ImmutableBiMap.copyOf(netherMap)
    }
}
