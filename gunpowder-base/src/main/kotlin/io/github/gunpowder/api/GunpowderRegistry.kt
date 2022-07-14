package io.github.gunpowder.api

import com.google.common.collect.BiMap
import io.github.gunpowder.api.types.Delegate
import io.github.gunpowder.api.types.ServerArgumentType
import io.github.gunpowder.api.types.SignType
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.registry.RegistryKey
import net.minecraft.world.World
import net.minecraft.world.dimension.DimensionType
import net.minecraft.world.gen.chunk.ChunkGenerator
import net.minecraft.world.level.LevelProperties
import net.minecraft.world.spawner.Spawner

interface GunpowderRegistry {
    // == Worlds/Dimensions ==

    /**
     * Note: Dimension Types do not persist naturally. While their dimensions are saved, they must be re-created
     *  by your mod to ensure the dimension loads correctly.
     */
    fun addDimensionType(dimensionTypeId: RegistryKey<DimensionType>, dimensionType: DimensionType)
    fun hasDimensionType(dimensionTypeId: RegistryKey<DimensionType>): Boolean
    fun removeDimensionType(dimensionTypeId: RegistryKey<DimensionType>)

    /**
     * Note: Worlds do not persist naturally. While their world data is saved, they must be re-created
     *  by your mod to ensure the world loads correctly, otherwise users will be moved to the overworld on load.
     * Worlds also do not save a custom spawn position! Keep track of this yourself though either a database, json file,
     *  or other method, and use World.setSpawn after calling addWorld.
     */
    fun addWorld(worldId: RegistryKey<World>, dimensionTypeId: RegistryKey<DimensionType>, chunkGenerator: ChunkGenerator, properties: LevelProperties, spawners: List<Spawner>): ServerWorld
    fun hasWorld(worldId: RegistryKey<World>): Boolean
    fun removeWorld(worldId: RegistryKey<World>)

    /**
     * Link two worlds by nether portal. This lasts until server shutdown.
     * Linking the vanilla dimensions will not work, and no world can be linked twice.
     */
    fun linkNether(overworld: RegistryKey<World>, nether: RegistryKey<World>)
    fun getLinkedWorlds(): BiMap<RegistryKey<World>, RegistryKey<World>>

    // == Config files ==
    fun <T> config(path: String, clazz: Class<T>): Delegate<T>

    // == Sign Types ==
    fun register(signType: SignType)

    // == Server-side argument types ==
    fun register(argumentType: ServerArgumentType<*>)
}

inline fun <reified T> GunpowderRegistry.config(path: String) = config(path, T::class.java)
