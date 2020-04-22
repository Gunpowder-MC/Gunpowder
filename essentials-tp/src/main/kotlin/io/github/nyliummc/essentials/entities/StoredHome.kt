package io.github.nyliummc.essentials.entities

import com.mojang.authlib.GameProfile
import net.minecraft.util.math.Vec3i

data class StoredHome(
        val user: GameProfile,
        val name: String,
        val location: Vec3i,
        val dimension: Int
)
