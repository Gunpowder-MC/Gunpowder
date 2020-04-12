package io.github.nyliummc.essentials.extension.teleport.impl

import io.github.nyliummc.essentials.extension.teleport.api.Teleportation
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import net.minecraft.world.dimension.DimensionType

class TeleportationImpl(override val destination: Vec3d, override val dimension: DimensionType, override val facing: Vec2f?) : Teleportation
