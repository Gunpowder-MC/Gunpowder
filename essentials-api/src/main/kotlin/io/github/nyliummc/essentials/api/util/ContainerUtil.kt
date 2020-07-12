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

package io.github.nyliummc.essentials.api.util

import net.fabricmc.fabric.impl.container.ServerPlayerEntitySyncHook
import net.fabricmc.fabric.mixin.container.ServerPlayerEntityAccessor
import net.minecraft.server.network.ServerPlayerEntity

object ContainerUtil {
    /**
     * Get a SyncID for containers
     */
    @JvmStatic
    fun getSyncId(player: ServerPlayerEntity): Int {
        val syncId: Int

        when (player) {
            is ServerPlayerEntitySyncHook -> {
                val serverPlayerEntitySyncHook = player as ServerPlayerEntitySyncHook
                syncId = serverPlayerEntitySyncHook.fabric_incrementSyncId()
            }
            is ServerPlayerEntityAccessor -> {
                syncId = ((player as ServerPlayerEntityAccessor).screenHandlerSyncId + 1) % 100
                (player as ServerPlayerEntityAccessor).screenHandlerSyncId = syncId
            }
            else -> throw RuntimeException("Neither ServerPlayerEntitySyncHook nor Accessor present! This should not happen!")
        }

        return syncId
    }
}
