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

import io.github.gunpowder.api.GunpowderMod
import io.github.gunpowder.entities.mc.ChestGuiContainer
import io.github.gunpowder.events.BlockPreBreakCallback
import io.github.gunpowder.mixin.cast.SignBlockEntityMixinCast_Base
import io.github.gunpowder.mod.AbstractGunpowderMod
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.block.AbstractSignBlock
import net.minecraft.block.entity.SignBlockEntity
import net.minecraft.util.ActionResult

object GunpowderEvents {
    private var counter = 0
    val guis = mutableListOf<ChestGuiContainer>()

    fun init() {
        BlockPreBreakCallback.EVENT.register(BlockPreBreakCallback { player, world, pos ->
            val state = world.getBlockState(pos)
            if (state.block is AbstractSignBlock) {
                val be = world.getBlockEntity(pos) as SignBlockEntity
                val cast = be as SignBlockEntityMixinCast_Base

                if (cast.isCustom &&
                    !player.hasPermissionLevel(4) &&
                    !cast.signType.conditionEvent(be, player) &&
                    !cast.isCreator(player)) {
                    return@BlockPreBreakCallback ActionResult.FAIL;
                }
            }
            return@BlockPreBreakCallback ActionResult.PASS
        })

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(ServerLifecycleEvents.EndDataPackReload { minecraftServer, serverResourceManager, b ->
            val gp = (GunpowderMod.instance as AbstractGunpowderMod)
            gp.reload();
        })

        ServerLifecycleEvents.SERVER_STOPPED.register(ServerLifecycleEvents.ServerStopped { server ->
            // Disable DB, unregister everything except commands
            GunpowderDatabase.disconnect()
        })

        ServerTickEvents.START_WORLD_TICK.register(ServerTickEvents.StartWorldTick {
            if (counter++ == 20) {
                counter = 0
                guis.forEach(ChestGuiContainer::update)
            }
        })
    }
}
