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

package io.github.gunpowder.modelhandlers

import io.github.gunpowder.api.GunpowderMod
import io.github.gunpowder.api.module.claims.dataholders.StoredClaim
import io.github.gunpowder.api.module.claims.dataholders.StoredClaimAuthorized
import io.github.gunpowder.models.ClaimAuthorizedTable
import io.github.gunpowder.models.ClaimTable
import net.minecraft.util.Identifier
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryKey
import net.minecraft.world.World
import org.jetbrains.exposed.sql.*
import java.util.*
import io.github.gunpowder.api.module.claims.modelhandlers.ClaimHandler as APIClaimHandler

object ClaimHandler : APIClaimHandler {
    private val db by lazy {
        GunpowderMod.instance.database
    }
    private val claimMap = mutableMapOf<RegistryKey<World>, MutableMap<ChunkPos, StoredClaim>>();
    private val claimAuthorizedMap = mutableMapOf<StoredClaim, MutableList<StoredClaimAuthorized>>();

    init {
        loadAllClaims()
    }

    private fun loadAllClaims() {
        val auths = db.transaction {
            val claims = ClaimTable.selectAll().map {
                val dimension = RegistryKey.of(Registry.DIMENSION, Identifier(it[ClaimTable.dimension]))
                val chunk = ChunkPos(it[ClaimTable.chunkX], it[ClaimTable.chunkZ])
                val cl = StoredClaim(it[ClaimTable.owner], chunk, dimension)
                Pair(cl, it[ClaimTable.id])
            }.toList()

            ClaimAuthorizedTable.selectAll().map {
                val claim = claims.first { c -> c.second == it[ClaimAuthorizedTable.claim] }.first
                StoredClaimAuthorized(claim, it[ClaimAuthorizedTable.user])
            }.toList()
        }.get()

        auths.forEach {
            claimMap.getOrPut(it.claim.dimension, ::mutableMapOf).putIfAbsent(it.claim.chunk, it.claim)
            claimAuthorizedMap.getOrPut(it.claim, ::mutableListOf).add(it)
        }
    }

    override fun isChunkClaimed(chunk: ChunkPos, dimension: RegistryKey<World>): Boolean {
        return claimMap.getOrPut(dimension, ::mutableMapOf)[chunk] != null
    }

    override fun createClaim(data: StoredClaim): Boolean {
        if (claimMap.getOrPut(data.dimension, ::mutableMapOf)[data.chunk] != null) {
            return false
        }

        db.transaction {
            val c = ClaimTable.insert {
                it[chunkX] = data.chunk.startX shr 4
                it[chunkZ] = data.chunk.startZ shr 4
                it[dimension] = data.dimension.value.toString()
                it[owner] = data.owner
            }

            ClaimAuthorizedTable.insert {
                it[claim] = c[ClaimTable.id]
                it[user] = data.owner
            }
        }

        return true
    }

    override fun getClaim(chunk: ChunkPos, dimension: RegistryKey<World>): StoredClaim {
        return claimMap[dimension]!![chunk]!!
    }

    override fun deleteClaim(chunk: ChunkPos, dimension: RegistryKey<World>): Boolean {
        if (!claimMap.getOrPut(dimension, ::mutableMapOf).containsKey(chunk)) {
            return false
        }

        val stored = claimMap[dimension]!![chunk]!!
        claimAuthorizedMap.remove(stored)
        claimMap[dimension]!!.remove(chunk)

        db.transaction {
            ClaimTable.deleteWhere {
                ClaimTable.chunkX.eq(chunk.startX shr 4)
                        .and(ClaimTable.chunkZ.eq(chunk.startZ shr 4))
                        .and(ClaimTable.dimension.eq(dimension.value.toString()))
            }
        }

        return true
    }

    override fun getClaimAllowed(chunk: ChunkPos, dimension: RegistryKey<World>): List<StoredClaimAuthorized> {
        return claimAuthorizedMap[claimMap[dimension]!![chunk]!!]!!.toList();
    }

    override fun addClaimAllowed(data: StoredClaimAuthorized): Boolean {
        if (claimAuthorizedMap[data.claim]!!.any { it.user == data.user }) {
            return false
        }

        claimAuthorizedMap[data.claim]!!.add(data);

        db.transaction {
            val c = ClaimTable.select {
                ClaimTable.chunkX.eq(data.claim.chunk.startX)
                        .and(ClaimTable.chunkZ.eq(data.claim.chunk.startZ shr 4))
                        .and(ClaimTable.dimension.eq(data.claim.dimension.value.toString()))
            }.first()

            ClaimAuthorizedTable.insert {
                it[claim] = c[ClaimTable.id]
                it[user] = data.user
            }
        }

        return true
    }

    override fun removeClaimAllowed(data: StoredClaimAuthorized): Boolean {
        if (claimAuthorizedMap[data.claim]!!.contains(data)) {
            claimAuthorizedMap[data.claim]!!.remove(data)

            db.transaction {
                val c = ClaimTable.select {
                    ClaimTable.chunkX.eq(data.claim.chunk.startX)
                            .and(ClaimTable.chunkZ.eq(data.claim.chunk.startZ shr 4))
                            .and(ClaimTable.dimension.eq(data.claim.dimension.value.toString()))
                }.first()

                ClaimAuthorizedTable.deleteWhere {
                    ClaimAuthorizedTable.claim.eq(c[ClaimTable.id])
                            .and(ClaimAuthorizedTable.user.eq(data.user))
                }
            }

            return true
        }
        return false
    }

    override fun getClaims(uuid: UUID): List<StoredClaim> {
        return claimMap.values.map { m -> m.values.filter { it.owner == uuid }.toList() }.toList().flatten()
    }
}
