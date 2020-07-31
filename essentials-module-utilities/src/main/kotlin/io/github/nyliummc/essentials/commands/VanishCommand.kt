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
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import io.github.nyliummc.essentials.api.builders.Command
import io.github.nyliummc.essentials.mixin.cast.PlayerVanish
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.LiteralText
import net.minecraft.util.Formatting

object VanishCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        Command.builder(dispatcher) {
            command("vanish") {
                requires { it.hasPermissionLevel(4) }
                literal("toggle") {
                    executes(::toggleVanish)
                }
                executes {
                    val player = it.source.player
                    player.sendMessage(
                            LiteralText(
                                    if ((player as PlayerVanish).isVanished)
                                        "You are vanished."
                                    else
                                        "You're not vanished."
                            ).formatted(Formatting.AQUA),
                            true
                    )
                    1
                }
            }
        }
    }

    //todo server ping -> players; hide vanished
    @Throws(CommandSyntaxException::class)
    private fun toggleVanish(ctx: CommandContext<ServerCommandSource>): Int {
        val player = ctx.source.player as PlayerVanish
        player.isVanished = !player.isVanished

        // Sending info to player
        ctx.source.player.sendMessage(
                LiteralText(
                        if (player.isVanished)
                            "Puff! You have vanished from the world."
                        else
                            "You are now unvanished."
                ).formatted(Formatting.AQUA),
                true
        )
        return 1
    }
}
