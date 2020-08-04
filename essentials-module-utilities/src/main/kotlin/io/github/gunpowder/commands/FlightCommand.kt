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
import io.github.ladysnake.pal.Pal
import io.github.ladysnake.pal.VanillaAbilities
import io.github.gunpowder.api.builders.Command
import net.minecraft.command.arguments.EntityArgumentType
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.LiteralText

object FlightCommand {
    val ESSENTIALS_ABILITY_FLY = Pal.getAbilitySource("essentials", "flight");

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        Command.builder(dispatcher) {
            command("flight", "fly") {
                requires { it.hasPermissionLevel(4) }

                executes(FlightCommand::toggleFlightSelf)
                argument("player", EntityArgumentType.player()) {
                    requires { it.hasPermissionLevel(4) }

                    executes(FlightCommand::toggleFlightOther)
                }
            }
        }
    }

    private fun toggleFlightSelf(commandContext: CommandContext<ServerCommandSource>): Int {
        // Set flight
        toggleFlight(commandContext.source.player)

        // Send feedback
        commandContext.source.sendFeedback(
                LiteralText("Successfully toggled flight"),
                false)

        return 1
    }

    private fun toggleFlightOther(commandContext: CommandContext<ServerCommandSource>): Int {
        // Get player
        val player = EntityArgumentType.getPlayer(commandContext, "player")

        // Set flight
        toggleFlight(player)

        // Send feedback
        commandContext.source.sendFeedback(
                LiteralText("Successfully toggled flight for ${player.displayName.asString()}"),
                false)

        return 1
    }

    private fun toggleFlight(player: ServerPlayerEntity) {
        if (ESSENTIALS_ABILITY_FLY.grants(player, VanillaAbilities.FLYING)) {
            ESSENTIALS_ABILITY_FLY.revokeFrom(player, VanillaAbilities.ALLOW_FLYING)
            ESSENTIALS_ABILITY_FLY.revokeFrom(player, VanillaAbilities.FLYING)
        } else {
            ESSENTIALS_ABILITY_FLY.grantTo(player, VanillaAbilities.ALLOW_FLYING)
            ESSENTIALS_ABILITY_FLY.grantTo(player, VanillaAbilities.FLYING)
        }
        player.sendAbilitiesUpdate()
    }
}
