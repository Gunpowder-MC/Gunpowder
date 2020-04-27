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

import net.minecraft.block.SignBlock
import net.minecraft.server.MinecraftServer
import net.minecraft.text.LiteralText
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import net.minecraft.world.dimension.DimensionType
import org.dynmap.DynmapChunk
import org.dynmap.DynmapWorld
import org.dynmap.common.DynmapListenerManager
import org.dynmap.common.DynmapPlayer
import org.dynmap.common.DynmapServerInterface
import org.dynmap.utils.MapChunkCache
import java.util.HashSet
import java.util.concurrent.Callable
import java.util.concurrent.Future


class FabricDynmapServer(private val minecraftServer: MinecraftServer) : DynmapServerInterface() {
    override fun scheduleServerTask(run: Runnable?, delay: Long) {
        TODO("Not yet implemented")
    }

    override fun <T> callSyncMethod(task: Callable<T>?): Future<T>? {
        TODO("Not yet implemented")
    }

    override fun getOnlinePlayers(): Array<DynmapPlayer?>? {
        return minecraftServer.playerManager.playerList.map(::FabricDynmapOnlinePlayer).toTypedArray()
    }

    override fun reload() {
        TODO("Not yet implemented")
    }

    override fun getPlayer(name: String?): DynmapPlayer? {
        return minecraftServer.playerManager.getPlayer(name)?.let(::FabricDynmapOnlinePlayer)
    }

    override fun getOfflinePlayer(name: String?): DynmapPlayer? {
        TODO("Not yet implemented")
    }

    override fun getIPBans(): Set<String?>? {
        return HashSet(minecraftServer.playerManager.ipBanList.names.asList())
    }

    override fun getServerName(): String? {
        return minecraftServer.serverMotd ?: return "Unknown Server"
    }

    override fun isPlayerBanned(pid: String?): Boolean {
        // TODO: Verify pid is the username
        return minecraftServer.playerManager.userBanList.names.contains(pid)
    }

    override fun stripChatColor(s: String?): String? {
        TODO("Not yet implemented")
    }

    override fun requestEventNotification(type: DynmapListenerManager.EventType?): Boolean {
        TODO("Not yet implemented")
    }

    override fun sendWebChatEvent(source: String?, name: String?, msg: String?): Boolean {
        TODO("Not yet implemented")
    }

    override fun broadcastMessage(msg: String?) {
        minecraftServer.playerManager.sendToAll(LiteralText(msg))
    }

    override fun getBiomeIDs(): Array<String?>? {
        // TODO: Support translation keys if needed
        return Registry.BIOME.ids.map(Identifier::toString).toTypedArray()
    }

    override fun getCacheHitRate(): Double {
        TODO("Not yet implemented")
    }

    override fun resetCacheStats() {
        TODO("Not yet implemented")
    }

    override fun getWorldByName(wname: String?): DynmapWorld? {
        val dtype = Registry.DIMENSION_TYPE.first { it.suffix == wname }
        return FabricDynmapWorld(minecraftServer.getWorld(dtype))
    }

    override fun checkPlayerPermissions(player: String?, perms: Set<String?>?): Set<String?>? {
        TODO("Not yet implemented")
    }

    override fun checkPlayerPermission(player: String?, perm: String?): Boolean {
        TODO("Not yet implemented")
    }

    override fun createMapChunkCache(w: DynmapWorld, chunks: List<DynmapChunk?>, blockdata: Boolean, highesty: Boolean, biome: Boolean, rawbiome: Boolean): MapChunkCache? {
        return FabricDynmapMapChunkCache(w, chunks)
    }

    override fun getMaxPlayers(): Int {
        return minecraftServer.maxPlayerCount
    }

    override fun getCurrentPlayers(): Int {
        return minecraftServer.currentPlayerCount
    }

    override fun getBlockIDAt(wname: String?, x: Int, y: Int, z: Int): Int {
        val dtype = Registry.DIMENSION_TYPE.first { it.suffix == wname }
        return Registry.BLOCK.getRawId(minecraftServer.getWorld(dtype).getBlockState(BlockPos(x, y, z)).block)
    }

    override fun isSignAt(wname: String?, x: Int, y: Int, z: Int): Int {
        val dtype = Registry.DIMENSION_TYPE.first { it.suffix == wname }
        return if(minecraftServer.getWorld(dtype).getBlockState(BlockPos(x, y, z)).block is SignBlock) 1 else 0
    }

    override fun getServerTPS(): Double {
        return 1.0 / minecraftServer.tickTime
    }

    override fun getServerIP(): String? {
        return minecraftServer.serverIp
    }
}
