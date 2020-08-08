/*
 * MIT License
 *
 * Copyright (c) GunpowderMC
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

package io.github.gunpowder.entities

import io.github.gunpowder.events.BlockPreBreakCallback
import io.github.gunpowder.mixin.cast.SignBlockEntityMixinCast_Base
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.block.AbstractSignBlock
import net.minecraft.block.entity.SignBlockEntity
import net.minecraft.util.ActionResult

object GunpowderEvents {
    fun init() {
        BlockPreBreakCallback.EVENT.register(BlockPreBreakCallback { player, world, pos ->
            val state = world.getBlockState(pos)
            if (state.block is AbstractSignBlock) {
                val be = world.getBlockEntity(pos) as SignBlockEntity
                val cast = be as SignBlockEntityMixinCast_Base
                if (cast.isCustom && be.editor != null && be.editor.uuid != player.uuid && !player.hasPermissionLevel(4) && !player.isCreative) {
                    return@BlockPreBreakCallback ActionResult.FAIL;
                }
            }
            return@BlockPreBreakCallback ActionResult.PASS
        })

        ServerLifecycleEvents.SERVER_STARTED.register(ServerLifecycleEvents.ServerStarted { server ->
            GunpowderDatabase.loadDatabase()
        })

        ServerLifecycleEvents.SERVER_STOPPED.register(ServerLifecycleEvents.ServerStopped { server ->
            // Disable DB, unregister everything except commands
            GunpowderDatabase.disconnect()
        })
    }
}
