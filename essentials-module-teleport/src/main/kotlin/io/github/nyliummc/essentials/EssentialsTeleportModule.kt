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

package io.github.nyliummc.essentials

import io.github.nyliummc.essentials.api.EssentialsMod
import io.github.nyliummc.essentials.api.EssentialsModule
import io.github.nyliummc.essentials.commands.*
import io.github.nyliummc.essentials.configs.TeleportConfig
import io.github.nyliummc.essentials.events.PlayerTeleportCallback

class EssentialsTeleportModule : EssentialsModule {
    override val name = "teleport"
    override val toggleable = true
    val essentials: EssentialsMod = EssentialsMod.instance

    override fun registerCommands() {
        essentials.registry.registerCommand(BackCommand::register)
        essentials.registry.registerCommand(HomeCommand::register)
        essentials.registry.registerCommand(RTPCommand::register)
        essentials.registry.registerCommand(SpawnCommand::register)
        essentials.registry.registerCommand(TPACommand::register)
        essentials.registry.registerCommand(WarpCommand::register)
    }

    override fun registerConfigs() {
        essentials.registry.registerConfig("essentials-teleport.yaml", TeleportConfig::class.java, "essentials-teleport.yaml")
    }

    override fun onInitialize() {
        PlayerTeleportCallback.EVENT.register(PlayerTeleportCallback { player, request ->
            BackCommand.lastPosition[player.uuid] = BackCommand.LastPosition(player.pos, player.dimension, player.rotationClient)
        })
    }
}
