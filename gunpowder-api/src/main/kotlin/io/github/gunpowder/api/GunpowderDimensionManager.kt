package io.github.gunpowder.api

import net.minecraft.server.world.ServerWorld
import net.minecraft.util.registry.RegistryKey
import net.minecraft.world.World
import net.minecraft.world.dimension.DimensionType
import net.minecraft.world.gen.chunk.ChunkGenerator

interface GunpowderDimensionManager {
    fun hasDimensionType(dimensionTypeId: RegistryKey<DimensionType>): Boolean
    fun addDimensionType(dimensionTypeId: RegistryKey<DimensionType>, dimensionType: DimensionType)
    fun removeDimensionType(dimensionTypeId: RegistryKey<DimensionType>)

    fun hasWorld(worldId: RegistryKey<World>): Boolean
    fun addWorld(worldId: RegistryKey<World>, dimensionTypeId: RegistryKey<DimensionType>, chunkGenerator: ChunkGenerator): ServerWorld
    fun removeWorld(worldId: RegistryKey<World>)
}
