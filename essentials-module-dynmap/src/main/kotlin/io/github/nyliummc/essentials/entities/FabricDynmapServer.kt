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

import com.mojang.authlib.GameProfile
import io.github.nyliummc.essentials.EssentialsDynmapModule
import net.minecraft.block.SignBlock
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.LiteralText
import net.minecraft.util.Identifier
import net.minecraft.util.Util
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryKey
import net.minecraft.world.World
import org.dynmap.DynmapChunk
import org.dynmap.DynmapCommonAPIListener
import org.dynmap.DynmapCore
import org.dynmap.DynmapWorld
import org.dynmap.common.DynmapListenerManager
import org.dynmap.common.DynmapPlayer
import org.dynmap.common.DynmapServerInterface
import org.dynmap.utils.MapChunkCache
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Future
import java.util.function.Supplier
import java.util.regex.Pattern


class FabricDynmapServer(private val server: MinecraftServer) : DynmapServerInterface() {
    private val patternControlCode: Pattern = Pattern.compile("(?i)[\\u00A7&][0-9A-FK-OR]")
    private val tickTaskMap: ConcurrentHashMap<Long, MutableList<Runnable>> = ConcurrentHashMap()
    private val fabricWorldMap: WeakHashMap<World, FabricDynmapWorld> = WeakHashMap<World, FabricDynmapWorld>()
    private val fabricPlayerMap: WeakHashMap<ServerPlayerEntity, FabricDynmapOnlinePlayer> = WeakHashMap<ServerPlayerEntity, FabricDynmapOnlinePlayer>()
    private val registered = mutableSetOf<DynmapListenerManager.EventType>()

    fun getWorld(world: ServerWorld): FabricDynmapWorld {
        return fabricWorldMap.computeIfAbsent(world) { FabricDynmapWorld(world) }
    }

    fun getUser(p: ServerPlayerEntity): FabricDynmapOnlinePlayer {
        return fabricPlayerMap.computeIfAbsent(p) { FabricDynmapOnlinePlayer(p) }
    }

    fun init(core: DynmapCore) {
        for (world in server.worlds) {
            val dw = getWorld(world)
            core.processWorldLoad(dw)
            EssentialsDynmapModule.core.listenerManager.processWorldEvent(DynmapListenerManager.EventType.WORLD_LOAD, dw)
        }
    }

    private fun getProfileByName(player: String): GameProfile? {
        return server.userCache.findByName(player)
    }

    override fun scheduleServerTask(run: Runnable, delay: Long) {
        val tick = server.ticks.toLong() + if (delay < 0) 0 else delay
        tickTaskMap.computeIfAbsent(tick) { ArrayList() }.add(run)
    }

    fun tick() {
        val list: List<Runnable>? = tickTaskMap.remove(server.ticks.toLong())
        if (list != null) {
            for (r in list) {
                r.run()
            }
        }
    }


    override fun <T> callSyncMethod(task: Callable<T>): Future<T?> {
        return server.submit(Supplier {
            try {
                task.call()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        })
    }

    override fun getOnlinePlayers(): Array<DynmapPlayer?>? {
        return server.playerManager.playerList.map(this::getUser).toTypedArray()
    }

    override fun reload() {
        // TODO("Not yet implemented")
    }

    override fun getPlayer(name: String?): DynmapPlayer? {
        return server.playerManager.getPlayer(name)?.let(this::getUser)
    }

    override fun getOfflinePlayer(name: String?): DynmapPlayer? {
        // TODO("Not yet implemented")
        return null
    }

    override fun getIPBans(): Set<String?>? {
        return HashSet(server.playerManager.ipBanList.names.asList())
    }

    override fun getServerName(): String? {
        return server.name
    }

    override fun isPlayerBanned(pid: String): Boolean {
        return server.playerManager.userBanList.contains(getProfileByName(pid));
    }

    override fun stripChatColor(s: String): String {
        return patternControlCode.matcher(s).replaceAll("");
    }

    override fun requestEventNotification(type: DynmapListenerManager.EventType): Boolean {
        if (registered.contains(type)) {
            return true
        }

        when (type) {
            DynmapListenerManager.EventType.WORLD_LOAD,
            DynmapListenerManager.EventType.WORLD_UNLOAD,
            DynmapListenerManager.EventType.PLAYER_JOIN,
            DynmapListenerManager.EventType.PLAYER_QUIT -> {
                // Implemented in mixins
            }

            DynmapListenerManager.EventType.WORLD_SPAWN_CHANGE -> {
                // TODO
                return false
            }

            DynmapListenerManager.EventType.PLAYER_BED_LEAVE -> {
                // TODO
                return false
            }
            DynmapListenerManager.EventType.PLAYER_CHAT -> {

                return false
            }
            DynmapListenerManager.EventType.SIGN_CHANGE -> {
                // TODO
                return false
            }
        }
        registered.add(type)
        return true
    }

    override fun sendWebChatEvent(source: String, name: String, msg: String): Boolean {
        server.sendSystemMessage(LiteralText("[$source/$name] $msg"), Util.NIL_UUID)
        return DynmapCommonAPIListener.fireWebChatEvent(source, name, msg)
    }

    override fun broadcastMessage(msg: String?) {
        server.sendSystemMessage(LiteralText("[Dynmap] $msg"), Util.NIL_UUID);
    }

    override fun getBiomeIDs(): Array<String?>? {
        // TODO: Support translation keys if needed
        return Registry.BIOME.ids.map(Identifier::toString).toTypedArray()
    }

    override fun getCacheHitRate(): Double {
        // TODO("Not yet implemented")
        return 0.0
    }

    override fun resetCacheStats() {
        // TODO("Not yet implemented")
    }

    override fun getWorldByName(wname: String): DynmapWorld? {
        return getWorld(server.getWorld(RegistryKey.of(Registry.DIMENSION, Identifier(wname)))!!);
    }

    override fun checkPlayerPermissions(player: String, perms: Set<String>?): Set<String?> {
        // TODO("Not yet implemented")
        return setOf()
    }

    override fun checkPlayerPermission(player: String?, perm: String?): Boolean {
        // TODO("Not yet implemented")
        return true
    }

    override fun createMapChunkCache(w: DynmapWorld, chunks: List<DynmapChunk>, blockdata: Boolean, highesty: Boolean, biome: Boolean, rawbiome: Boolean): MapChunkCache? {
        val cache = FabricDynmapMapChunkCache(w as FabricDynmapWorld, w.world, chunks)
        cache.setChunkDataTypes(blockdata, biome, highesty, rawbiome)
        return cache
    }

    override fun getMaxPlayers(): Int {
        return server.maxPlayerCount
    }

    override fun getCurrentPlayers(): Int {
        return server.currentPlayerCount
    }

    override fun getBlockIDAt(wname: String?, x: Int, y: Int, z: Int): Int {
        val dtype = RegistryKey.of(Registry.DIMENSION, Identifier(wname))
        return Registry.BLOCK.getRawId(server.getWorld(dtype)!!.getBlockState(BlockPos(x, y, z)).block)
    }

    override fun isSignAt(wname: String?, x: Int, y: Int, z: Int): Int {
        val dtype = RegistryKey.of(Registry.DIMENSION, Identifier(wname))
        return if (server.getWorld(dtype)!!.getBlockState(BlockPos(x, y, z)).block is SignBlock) 1 else 0
    }

    override fun getServerTPS(): Double {
        // Get from module-utilities if available
        return 1.0 / server.tickTime
    }

    override fun getServerIP(): String? {
        return server.serverIp
    }
}
