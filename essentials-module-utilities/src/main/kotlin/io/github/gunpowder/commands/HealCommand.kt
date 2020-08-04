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

package io.github.gunpowder.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import io.github.gunpowder.api.builders.Command
import net.minecraft.command.arguments.EntityArgumentType
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.LiteralText

object HealCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        Command.builder(dispatcher) {
            command("heal") {
                requires { it.hasPermissionLevel(4) }
                executes(HealCommand::healSelf)

                argument("player", EntityArgumentType.player()) {
                    requires { it.hasPermissionLevel(4) }
                    executes(HealCommand::healOther)
                }
            }
        }
    }

    private fun healSelf(context: CommandContext<ServerCommandSource>): Int {
        healPlayer(context.source.player)
        context.source.sendFeedback(
                LiteralText("Successfully healed"),
                false)
        return 1
    }

    private fun healOther(context: CommandContext<ServerCommandSource>): Int {
        val player = EntityArgumentType.getPlayer(context, "player")
        healPlayer(player)
        context.source.sendFeedback(
                LiteralText("Successfully healed ${player.displayName.asString()}"),
                false)
        return 1
    }

    private fun healPlayer(player: ServerPlayerEntity) {
        player.health = player.maxHealth;
        player.hungerManager.foodLevel = 20;
        player.extinguish();
        player.clearStatusEffects();
    }
}
