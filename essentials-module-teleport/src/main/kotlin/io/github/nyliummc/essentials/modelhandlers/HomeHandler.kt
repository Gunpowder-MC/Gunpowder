/*
 * MIT License
 *
 * Copyright (c) NyliumMC
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

package io.github.nyliummc.essentials.modelhandlers

import io.github.nyliummc.essentials.api.EssentialsMod
import io.github.nyliummc.essentials.api.module.teleport.dataholders.StoredHome
import io.github.nyliummc.essentials.configs.TeleportConfig
import io.github.nyliummc.essentials.models.HomeTable
import net.minecraft.util.math.Vec3i
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import io.github.nyliummc.essentials.api.module.teleport.modelhandlers.HomeHandler as APIHomeHandler

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
