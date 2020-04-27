package io.github.nyliummc.essentials.entities

import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.world.dimension.DimensionType
import org.dynmap.DynmapLocation
import org.dynmap.common.DynmapPlayer
import java.net.InetSocketAddress
import java.net.SocketAddress


class FabricDynmapOnlinePlayer(private val player: ServerPlayerEntity) : DynmapPlayer {
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
        return player.world.getDimension().type.suffix
    }

    override fun getAddress(): InetSocketAddress? {
        val address = player.networkHandler.getConnection().address as SocketAddress
        return if (address is InetSocketAddress) {
            address
        } else {
            null
        }
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
        val pos = player.spawnPosition
        return DynmapLocation(DimensionType.OVERWORLD.suffix, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
    }

    override fun getLastLoginTime(): Long {
        TODO("Not yet implemented")
    }

    override fun getFirstLoginTime(): Long {
        TODO("Not yet implemented")
    }

    override fun isInvisible(): Boolean {
        return player.isInvisible
    }

    override fun getSortWeight(): Int {
        TODO("Not yet implemented")
    }

    override fun setSortWeight(wt: Int) {
        TODO("Not yet implemented")
    }

    override fun hasPrivilege(privid: String): Boolean {
        // TODO
        return isOp
    }

    override fun sendMessage(msg: String) {
        TODO("Not yet implemented")
    }

    override fun isConnected(): Boolean {
        return true
    }

    override fun isOp(): Boolean {
        return player.allowsPermissionLevel(player.server.opPermissionLevel)
    }

    override fun hasPermissionNode(node: String): Boolean {
        // TODO
        return isOp
    }

}
