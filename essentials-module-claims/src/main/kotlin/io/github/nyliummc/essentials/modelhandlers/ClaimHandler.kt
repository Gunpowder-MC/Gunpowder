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

import io.github.nyliummc.essentials.api.module.claims.dataholders.StoredClaim
import io.github.nyliummc.essentials.api.module.claims.dataholders.StoredClaimAuthorized
import io.github.nyliummc.essentials.models.ClaimAuthorizedTable
import io.github.nyliummc.essentials.models.ClaimTable
import net.minecraft.util.Identifier
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryKey
import net.minecraft.world.World
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import io.github.nyliummc.essentials.api.module.claims.modelhandlers.ClaimHandler as APIClaimHandler

object ClaimHandler : APIClaimHandler {
    private val claimMap = mutableMapOf<RegistryKey<World>, MutableMap<ChunkPos, StoredClaim>>();
    private val claimAuthorizedMap = mutableMapOf<StoredClaim, MutableList<StoredClaimAuthorized>>();

    init {
        loadAllClaims()

        ClaimTable.selectAll().forEach {
            val chunk = ChunkPos(it[ClaimTable.chunkX], it[ClaimTable.chunkZ]);
            val dimension = RegistryKey.of(Registry.DIMENSION, Identifier(it[ClaimTable.dimension]))
            claimMap.getOrPut(dimension, ::mutableMapOf)[chunk] = StoredClaim(it[ClaimTable.owner], chunk, dimension)
        }
    }

    private fun loadAllClaims() {
        val claimsTemp = mutableMapOf<Int, StoredClaim>()

        transaction {
            ClaimAuthorizedTable.selectAll().forEach {
                val claim = claimsTemp.getOrPut(it[ClaimAuthorizedTable.claim]) {
                    val c = ClaimTable.select { ClaimTable.id.eq(it[ClaimAuthorizedTable.claim]) }.first()
                    val dimension = RegistryKey.of(Registry.DIMENSION, Identifier(c[ClaimTable.dimension]))
                    val chunk = ChunkPos(c[ClaimTable.chunkX], c[ClaimTable.chunkZ])
                    val cl = StoredClaim(c[ClaimTable.owner], chunk, dimension)
                    claimMap.getOrPut(dimension, ::mutableMapOf)[chunk] = cl
                    cl
                }
                claimAuthorizedMap[claim]!!.add(StoredClaimAuthorized(claim, it[ClaimAuthorizedTable.user]))
            }
        }
    }

    override fun isChunkClaimed(chunk: ChunkPos, dimension: RegistryKey<World>): Boolean {
        return claimMap.getOrPut(dimension, ::mutableMapOf)[chunk] != null
    }

    override fun createClaim(data: StoredClaim): Boolean {
        if (claimMap.getOrPut(data.dimension, ::mutableMapOf)[data.chunk] != null) {
            return false
        }

        transaction {
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

        transaction {
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

        transaction {
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

            transaction {
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
}
