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

package io.github.nyliummc.essentials.entities

import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.LiteralText
import net.minecraft.world.dimension.DimensionType
import org.dynmap.DynmapLocation
import org.dynmap.common.DynmapPlayer
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.*


class FabricDynmapOnlinePlayer(private val player: ServerPlayerEntity) : DynmapPlayer {
    private var weight = 0

    override fun getName(): String {
        return player.name.string
    }

    override fun getDisplayName(): String {
        return player.displayName.string
    }

    override fun isOnline(): Boolean {
        return true
    }

    override fun getLocation(): DynmapLocation {
        return DynmapLocation(this.world, player.x, player.y, player.z)
    }

    override fun getWorld(): String {
        return player.world.dimensionRegistryKey.value.path
    }

    override fun getAddress(): InetSocketAddress? {
        val address = player.networkHandler.getConnection().address as SocketAddress
        return address as? InetSocketAddress
    }

    override fun isSneaking(): Boolean {
        return player.isSneaky
    }

    override fun getHealth(): Double {
        return player.health.toDouble()
    }

    override fun getArmorPoints(): Int {
        return player.armor
    }

    override fun getBedSpawnLocation(): DynmapLocation {
        val pos = player.spawnPointPosition ?: player.server.overworld.spawnPos
        return DynmapLocation(player.spawnPointDimension.value.path, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
    }

    override fun getLastLoginTime(): Long {
        // TODO("Not yet implemented")
        return 0
    }

    override fun getFirstLoginTime(): Long {
        // TODO("Not yet implemented")
        return 0
    }

    override fun isInvisible(): Boolean {
        return player.isInvisible
    }

    override fun getSortWeight(): Int {
        // TODO("Not yet implemented")
        return weight
    }

    override fun setSortWeight(wt: Int) {
        weight = wt
    }

    override fun hasPrivilege(privid: String): Boolean {
        // TODO
        return isOp
    }

    override fun sendMessage(msg: String) {
        player.sendMessage(LiteralText(msg), false)
    }

    override fun isConnected(): Boolean {
        return true
    }

    override fun isOp(): Boolean {
        return player.hasPermissionLevel(player.server.opPermissionLevel)
    }

    override fun hasPermissionNode(node: String): Boolean {
        // TODO
        return isOp
    }

    override fun getSkinURL(): String? {
        // TODO("Not yet implemented")
        return null
    }

    override fun getUUID(): UUID? {
        return player.uuid
    }

}
