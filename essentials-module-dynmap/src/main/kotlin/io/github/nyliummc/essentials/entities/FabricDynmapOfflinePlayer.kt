package io.github.nyliummc.essentials.entities

import org.dynmap.DynmapLocation
import org.dynmap.common.DynmapPlayer
import java.net.InetSocketAddress

class FabricDynmapOfflinePlayer(private val name: String) : DynmapPlayer {
    override fun isSneaking(): Boolean {
        return false
    }

    override fun getName(): String {
        return name
    }

    override fun isOp(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isConnected(): Boolean {
        return false
    }

    override fun getFirstLoginTime(): Long {
        TODO("Not yet implemented")
    }

    override fun hasPrivilege(p0: String?): Boolean {
        TODO("Not yet implemented")
    }

    override fun getLastLoginTime(): Long {
        TODO("Not yet implemented")
    }

    override fun getHealth(): Double {
        return 20.0
    }

    override fun hasPermissionNode(p0: String?): Boolean {
        TODO("Not yet implemented")
    }

    override fun getArmorPoints(): Int {
        return 0
    }

    override fun getWorld(): String {
        TODO("Not yet implemented")
    }

    override fun isOnline(): Boolean {
        return false
    }

    override fun sendMessage(p0: String?) {
        TODO("Not yet implemented")
    }

    override fun isInvisible(): Boolean {
        return false
    }

    override fun getLocation(): DynmapLocation {
        TODO("Not yet implemented")
    }

    override fun getBedSpawnLocation(): DynmapLocation? {
        return null
    }

    override fun getDisplayName(): String {
        return name
    }

    override fun getSortWeight(): Int {
        TODO("Not yet implemented")
    }

    override fun setSortWeight(p0: Int) {
        TODO("Not yet implemented")
    }

    override fun getAddress(): InetSocketAddress? {
        return null
    }
}
