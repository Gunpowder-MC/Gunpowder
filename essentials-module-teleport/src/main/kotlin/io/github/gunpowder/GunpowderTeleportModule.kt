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

package io.github.gunpowder

import io.github.gunpowder.api.GunpowderMod
import io.github.gunpowder.api.GunpowderModule
import io.github.gunpowder.commands.*
import io.github.nyliummc.essentials.commands.*
import io.github.gunpowder.configs.TeleportConfig
import io.github.gunpowder.events.PlayerDeathCallback
import io.github.gunpowder.events.PlayerTeleportCallback
import io.github.gunpowder.modelhandlers.HomeHandler
import io.github.gunpowder.modelhandlers.WarpHandler
import io.github.gunpowder.models.HomeTable
import io.github.gunpowder.models.WarpTable
import java.util.function.Supplier
import io.github.gunpowder.api.module.teleport.modelhandlers.HomeHandler as APIHomeHandler
import io.github.gunpowder.api.module.teleport.modelhandlers.WarpHandler as APIWarpHandler

class GunpowderTeleportModule : GunpowderModule {
    override val name = "teleport"
    override val toggleable = true
    val gunpowder: GunpowderMod = GunpowderMod.instance

    override fun registerCommands() {
        gunpowder.registry.registerCommand(BackCommand::register)
        gunpowder.registry.registerCommand(HomeCommand::register)
        gunpowder.registry.registerCommand(RTPCommand::register)
        gunpowder.registry.registerCommand(SpawnCommand::register)
        gunpowder.registry.registerCommand(TPACommand::register)
        gunpowder.registry.registerCommand(WarpCommand::register)
    }

    override fun registerConfigs() {
        gunpowder.registry.registerConfig("essentials-teleport.yaml", TeleportConfig::class.java, "essentials-teleport.yaml")
    }

    override fun registerEvents() {
        PlayerTeleportCallback.EVENT.register(PlayerTeleportCallback { player, _ ->
            BackCommand.lastPosition[player.uuid] = BackCommand.LastPosition(player.pos, player.world, player.rotationClient)
        })
        PlayerDeathCallback.EVENT.register(PlayerDeathCallback { player, _ ->
            BackCommand.lastPosition[player.uuid] = BackCommand.LastPosition(player.pos, player.world, player.rotationClient)
        })
    }

    override fun onInitialize() {
        gunpowder.registry.registerTable(WarpTable)
        gunpowder.registry.registerTable(HomeTable)
        gunpowder.registry.registerModelHandler(APIWarpHandler::class.java, Supplier { WarpHandler })
        gunpowder.registry.registerModelHandler(APIHomeHandler::class.java, Supplier { HomeHandler })
    }
}
