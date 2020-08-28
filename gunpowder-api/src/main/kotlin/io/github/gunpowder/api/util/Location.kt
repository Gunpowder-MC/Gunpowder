package io.github.gunpowder.api.util

import io.github.gunpowder.api.GunpowderMod
import net.minecraft.entity.Entity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryKey

data class Location(
        val position: Vec3d,
        val rotation: Vec2f,
        val dimension: Identifier
) {
    companion object {
        @JvmStatic
        fun of(entity: Entity) = Location(entity.pos, Vec2f(entity.yaw, entity.pitch), entity.world.registryKey.value)
    }

    val world: ServerWorld?
        get() = GunpowderMod.instance.server.getWorld(RegistryKey.of(Registry.DIMENSION, this.dimension))
}

