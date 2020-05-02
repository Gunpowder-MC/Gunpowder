package io.github.nyliummc.essentials.modelhandlers

import com.mojang.authlib.GameProfile
import io.github.nyliummc.essentials.api.EssentialsMod
import io.github.nyliummc.essentials.api.module.teleport.dataholders.StoredHome
import io.github.nyliummc.essentials.configs.TeleportConfig
import io.github.nyliummc.essentials.models.HomeTable
import net.minecraft.util.math.Vec3i
import org.jetbrains.exposed.sql.*
import io.github.nyliummc.essentials.api.module.teleport.modelhandlers.HomeHandler as APIHomeHandler
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object HomeHandler : APIHomeHandler {
    private val cache: MutableMap<UUID, MutableMap<String, StoredHome>> = mutableMapOf()
    val homeLimit by lazy {
        EssentialsMod.instance.registry.getConfig(TeleportConfig::class.java).maxHomes
    }

    init {
        loadEntries()
    }

    private fun loadEntries() {
        transaction {
            val homes = HomeTable.selectAll()
            val owners = homes.map { it[HomeTable.owner] }.toList()
            owners.forEach { owner ->
                cache[owner] = homes.filter { it[HomeTable.owner] == owner }.map {
                    it[HomeTable.name] to
                            StoredHome(
                                    owner,
                                    it[HomeTable.name],
                                    Vec3i(it[HomeTable.x], it[HomeTable.y], it[HomeTable.z]),
                                    it[HomeTable.dimension]
                            )
                }.toMap().toMutableMap()
            }
        }
    }

    override fun getHome(user: UUID, home: String): StoredHome? {
        return cache[user]?.get(home)
    }

    override fun getHomes(user: UUID): Map<String, StoredHome> {
        return cache[user] ?: mapOf()
    }

    override fun newHome(home: StoredHome): Boolean {
        val c = cache[home.user] ?: mutableMapOf()

        if (c.size >= homeLimit) {
            if (c[home.name] != null) {
                return false
            }
        }

        c[home.name] = home
        cache[home.user] = c

        transaction {
            HomeTable.insert {
                it[owner] = home.user
                it[name] = home.name
                it[x] = home.location.x
                it[y] = home.location.y
                it[z] = home.location.z
                it[dimension] = home.dimension
            }
        }

        return true
    }

    override fun delHome(player: UUID, home: String): Boolean {
        val c = cache[player] ?: mutableMapOf()

        if (c[home] == null) {
            return false
        }

        c.remove(home)
        cache[player] = c
        transaction {
            HomeTable.deleteWhere {
                HomeTable.owner.eq(player).and(HomeTable.name.eq(home))
            }
        }
        return true
    }
}
