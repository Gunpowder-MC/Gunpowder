package io.github.nyliummc.essentials.util

import com.mojang.authlib.GameProfile
import net.minecraft.util.math.Vec3i

/*
 * Keeps track of last location teleported from for /back
 */
object TPCache {
    private val cache: MutableMap<GameProfile, Pair<Vec3i, Int>> = mutableMapOf()

    fun setLastLocation(player: GameProfile, location: Vec3i, dimension: Int) {
        cache[player] = Pair(location, dimension)
    }

    fun getLastLocation(player: GameProfile): Pair<Vec3i, Int> {
        val location = cache[player]
        cache.remove(player)
        return location!!
    }
}