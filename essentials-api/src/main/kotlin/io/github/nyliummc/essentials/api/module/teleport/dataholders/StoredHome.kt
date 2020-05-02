package io.github.nyliummc.essentials.api.module.teleport.dataholders

import net.minecraft.util.math.Vec3i
import java.util.*

data class StoredHome(
        val user: UUID,
        val name: String,
        val location: Vec3i,
        val dimension: Int
)
