package io.github.nyliummc.essentials.api.util

import net.fabricmc.fabric.impl.container.ServerPlayerEntitySyncHook
import net.fabricmc.fabric.mixin.container.ServerPlayerEntityAccessor
import net.minecraft.server.network.ServerPlayerEntity

object ContainerUtil {
    @JvmStatic
    fun getSyncId(player: ServerPlayerEntity): Int {
        var syncId: Int

        when (player) {
            is ServerPlayerEntitySyncHook -> {
                val serverPlayerEntitySyncHook = player as ServerPlayerEntitySyncHook
                syncId = serverPlayerEntitySyncHook.fabric_incrementSyncId()
            }
            is ServerPlayerEntityAccessor -> {
                syncId = ((player as ServerPlayerEntityAccessor).containerSyncId + 1) % 100
                (player as ServerPlayerEntityAccessor).containerSyncId = syncId
            }
            else -> throw RuntimeException("Neither ServerPlayerEntitySyncHook nor Accessor present! This should not happen!")
        }

        return syncId
    }
}
