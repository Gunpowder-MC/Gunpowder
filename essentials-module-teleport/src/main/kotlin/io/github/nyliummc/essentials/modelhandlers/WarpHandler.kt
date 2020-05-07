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

import io.github.nyliummc.essentials.api.module.teleport.dataholders.StoredWarp
import io.github.nyliummc.essentials.models.WarpTable
import net.minecraft.util.math.Vec3i
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import io.github.nyliummc.essentials.api.module.teleport.modelhandlers.WarpHandler as APIWarpHandler

object WarpHandler : APIWarpHandler {
    val cache = mutableMapOf<String, StoredWarp>()

    init {
        loadEntries()
    }

    private fun loadEntries() {
        transaction {
            cache.putAll(WarpTable.selectAll().map {
                it[WarpTable.name] to StoredWarp(
                        it[WarpTable.name],
                        Vec3i(it[WarpTable.x], it[WarpTable.y], it[WarpTable.z]),
                        it[WarpTable.dimension])
            }.toMap())
        }
    }

    override fun getWarp(name: String): StoredWarp? {
        return cache[name]
    }

    override fun getWarps(): Map<String, StoredWarp> {
        return cache.toMap()
    }

    override fun delWarp(warp: String): Boolean {
        if (cache.containsKey(warp)) {
            transaction {
                WarpTable.deleteWhere {
                    WarpTable.name.eq(warp)
                }
            }
            cache.remove(warp)
            return true
        }
        return false
    }

    override fun newWarp(warp: StoredWarp): Boolean {
        if (!cache.containsKey(warp.name)) {
            cache[warp.name] = warp
            transaction {
                WarpTable.insert {
                    it[WarpTable.name] = warp.name
                    it[WarpTable.x] = warp.location.x
                    it[WarpTable.y] = warp.location.y
                    it[WarpTable.z] = warp.location.z
                    it[WarpTable.dimension] = warp.dimension
                }
            }
            return true
        }
        return false
    }
}
