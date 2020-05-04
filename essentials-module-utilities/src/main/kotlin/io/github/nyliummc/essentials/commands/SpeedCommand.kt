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

package io.github.nyliummc.essentials.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import io.github.ladysnake.pal.Pal
import io.github.ladysnake.pal.VanillaAbilities
import io.github.nyliummc.essentials.api.builders.Command
import net.minecraft.command.arguments.EntityArgumentType
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.LiteralText

object SpeedCommand {
    val ESSENTIALS_ABILITY_SPEED = Pal.getAbilitySource("essentials", "speed");

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        Command.builder(dispatcher) {
            command("speed") {
                requires { it.hasPermissionLevel(4) }
                executes(::clearSpeedSelf)

                argument("player", EntityArgumentType.player()) {
                    requires { it.hasPermissionLevel(4) }

                    executes(::clearSpeedOther)
                }

                argument("speed", DoubleArgumentType.doubleArg(0.0, 10.0)) {
                    requires { it.hasPermissionLevel(4) }
                    executes(::setSpeedSelf)

                    argument("player", EntityArgumentType.player()) {
                        requires { it.hasPermissionLevel(4) }

                        executes(::setSpeedOther)
                    }
                }
            }
        }
    }

    private fun clearSpeedSelf(context: CommandContext<ServerCommandSource>): Int {
        setSpeed(context.source.player, 0.05)

        // Send feedback
        context.source.sendFeedback(
                LiteralText("Successfully reset speed"),
                false)

        return 1
    }

    private fun clearSpeedOther(context: CommandContext<ServerCommandSource>): Int {
        val player = EntityArgumentType.getPlayer(context, "player")
        setSpeed(player, 0.05)

        // Send feedback
        context.source.sendFeedback(
                LiteralText("Successfully reset speed for ${player.displayName.asString()}"),
                false)

        return 1
    }

    private fun setSpeedSelf(context: CommandContext<ServerCommandSource>): Int {
        val speed = DoubleArgumentType.getDouble(context, "speed")
        setSpeed(context.source.player, speed)

        // Send feedback
        context.source.sendFeedback(
                LiteralText("Successfully set speed to $speed"),
                false)

        return 1
    }

    private fun setSpeedOther(context: CommandContext<ServerCommandSource>): Int {
        val player = EntityArgumentType.getPlayer(context, "player")
        val speed = DoubleArgumentType.getDouble(context, "speed")
        setSpeed(player, speed)

        // Send feedback
        context.source.sendFeedback(
                LiteralText("Successfully set speed for ${player.displayName.asString()} to $speed"),
                false)

        return 1
    }

    private fun setSpeed(player: ServerPlayerEntity, speed: Double) {
        // TODO: Use PAL once it supports this
        player.abilities.flySpeed = speed.toFloat()
        player.abilities.walkSpeed = 2*speed.toFloat()
        player.sendAbilitiesUpdate()
    }
}
