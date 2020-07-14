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
import net.minecraft.util.math.ChunkPos
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import io.github.nyliummc.essentials.api.module.claims.modelhandlers.ClaimHandler as APIClaimHandler

object ClaimHandler : APIClaimHandler {
    private val claimMap = mutableMapOf<ChunkPos, StoredClaim>();
    private val claimAuthorizedMap = mutableMapOf<StoredClaim, MutableList<StoredClaimAuthorized>>();

    init {
        loadAllClaims()
    }

    private fun loadAllClaims() {
        val claims = transaction {
            ClaimTable.selectAll().map {
                val chunk = ChunkPos(it[ClaimTable.chunkX], it[ClaimTable.chunkZ]);
                chunk to StoredClaim(it[ClaimTable.owner], chunk)
            }.toMap()
        }

        val auth = transaction {
            ClaimAuthorizedTable.selectAll().map {
                val chunk = ChunkPos(it[ClaimAuthorizedTable.claimX], it[ClaimAuthorizedTable.claimZ]);
                claims[chunk]!! to StoredClaimAuthorized(claims[chunk]!!, it[ClaimAuthorizedTable.user])
            }
        }

        claimMap.putAll(claims)
        auth.forEach {
            if (!claimAuthorizedMap.containsKey(it.first)) {
                claimAuthorizedMap[it.first] = mutableListOf(it.second)
            } else {
                claimAuthorizedMap[it.first]!!.add(it.second)
            }
        }
    }

    override fun isChunkClaimed(chunk: ChunkPos): Boolean {
        return claimMap[chunk] != null
    }

    override fun createClaim(data: StoredClaim): Boolean {
        if (claimMap[data.chunk] != null) {
            return false
        }

        transaction {
            ClaimTable.insert {
                it[chunkX] = data.chunk.startX shr 4
                it[chunkZ] = data.chunk.startZ shr 4
                it[owner] = data.owner
            }

            ClaimAuthorizedTable.insert {
                it[claimX] = data.chunk.startX shr 4
                it[claimZ] = data.chunk.startZ shr 4
                it[user] = data.owner
            }
        }

        return true
    }

    override fun getClaim(chunk: ChunkPos): StoredClaim {
        return claimMap[chunk]!!
    }

    override fun deleteClaim(chunk: ChunkPos): Boolean {
        if (!claimMap.containsKey(chunk)) {
            return false
        }

        val stored = claimMap[chunk]!!
        claimAuthorizedMap.remove(stored)
        claimMap.remove(chunk)

        transaction {
            ClaimAuthorizedTable.deleteWhere { ClaimAuthorizedTable.claimX.eq(chunk.startX shr 4).and(ClaimAuthorizedTable.claimZ.eq(chunk.startZ shr 4)) }
            ClaimTable.deleteWhere { ClaimTable.chunkX.eq(chunk.startX shr 4).and(ClaimTable.chunkZ.eq(chunk.startZ shr 4)) }
        }

        return true
    }

    override fun getClaimAllowed(chunk: ChunkPos): List<StoredClaimAuthorized> {
        return claimAuthorizedMap[claimMap[chunk]!!]!!.toList();
    }

    override fun addClaimAllowed(data: StoredClaimAuthorized): Boolean {
        if (claimAuthorizedMap[data.claim]!!.any { it.user == data.user }) {
            return false
        }

        claimAuthorizedMap[data.claim]!!.add(data);

        transaction {
            ClaimAuthorizedTable.insert {
                it[claimX] = data.claim.chunk.startX shr 4
                it[claimZ] = data.claim.chunk.startZ shr 4
                it[user] = data.user
            }
        }

        return true
    }

    override fun removeClaimAllowed(data: StoredClaimAuthorized): Boolean {
        if (claimAuthorizedMap[data.claim]!!.contains(data)) {
            claimAuthorizedMap[data.claim]!!.remove(data)

            transaction {
                ClaimAuthorizedTable.deleteWhere {
                    ClaimAuthorizedTable.claimX.eq(data.claim.chunk.startX shr 4).and(ClaimAuthorizedTable.claimZ.eq(data.claim.chunk.startZ shr 4)).and(ClaimAuthorizedTable.user.eq(data.user))
                }
            }

            return true
        }
        return false
    }

    override fun getClaims(uuid: UUID): List<StoredClaim> {
        return claimMap.values.filter { it.owner == uuid }.toList()
    }
}
