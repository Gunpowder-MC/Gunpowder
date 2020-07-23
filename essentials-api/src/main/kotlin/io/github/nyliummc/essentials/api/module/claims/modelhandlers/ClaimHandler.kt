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

package io.github.nyliummc.essentials.api.module.claims.modelhandlers

import io.github.nyliummc.essentials.api.module.claims.dataholders.StoredClaim
import io.github.nyliummc.essentials.api.module.claims.dataholders.StoredClaimAuthorized
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.registry.RegistryKey
import net.minecraft.world.World
import java.util.*

interface ClaimHandler {
    fun isChunkClaimed(chunk: ChunkPos, dimension: RegistryKey<World>): Boolean

    fun createClaim(data: StoredClaim): Boolean
    fun getClaim(chunk: ChunkPos, dimension: RegistryKey<World>): StoredClaim
    fun deleteClaim(chunk: ChunkPos, dimension: RegistryKey<World>): Boolean

    fun getClaimAllowed(chunk: ChunkPos, dimension: RegistryKey<World>): List<StoredClaimAuthorized>
    fun addClaimAllowed(data: StoredClaimAuthorized): Boolean
    fun removeClaimAllowed(data: StoredClaimAuthorized): Boolean
    fun getClaims(uuid: UUID): List<StoredClaim>
}
