package io.github.nyliummc.essentials.modelcache

import com.mojang.authlib.GameProfile
import io.github.nyliummc.essentials.entities.StoredHome
import io.github.nyliummc.essentials.models.HomeTable
import net.minecraft.util.math.Vec3i
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object HomeCache {
    private val cache: MutableMap<GameProfile, MutableMap<String, StoredHome>> = mutableMapOf()

    fun getHome(profile: GameProfile, home: String): StoredHome? {
        return cache[profile]?.get(home) ?: getHomes(profile)[home]
    }

    fun newHome(home: StoredHome): Boolean {
        val c = getHomes(home.user).toMutableMap()

        // TODO: Fetch from config instead of hardcoded 10
        // Old code: EssentialsMod.config.tp.homeLimit
        if (c.size >= 10)
            if (c[home.name] != null) {
                return false
            }
        c[home.name] = home
        cache[home.user] = c
        transaction {
            HomeTable.insert {
                it[owner] = home.user.id
                it[name] = home.name
                it[x] = home.location.x
                it[y] = home.location.y
                it[z] = home.location.z
                it[dimension] = home.dimension
            }
        }
        return true
    }

    fun delHome(player: GameProfile, home: String): Boolean {
        val c = getHomes(player).toMutableMap()
        if (c[home] == null) {
            return false
        }
        c.remove(home)
        cache[player] = c
        transaction {
            HomeTable.deleteWhere {
                HomeTable.owner.eq(player.id).and(HomeTable.name.eq(home))
            }
        }
        return true
    }

    fun getHomes(player: GameProfile): Map<String, StoredHome> {
        val c = cache[player] ?: transaction {
            val homes = HomeTable.select { HomeTable.owner.eq(player.id) }.toList()
            homes.map {
                it[HomeTable.name] to
                        StoredHome(
                                player,
                                it[HomeTable.name],
                                Vec3i(it[HomeTable.x], it[HomeTable.y], it[HomeTable.z]),
                                it[HomeTable.dimension]
                        )
            }.toMap().toMutableMap()
        }
        cache[player] = c
        return c.toMap()
    }
}