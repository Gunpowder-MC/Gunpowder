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
import io.github.nyliummc.essentials.api.EssentialsMod
import io.github.nyliummc.essentials.api.builders.Command
import io.github.nyliummc.essentials.api.builders.Text
import io.github.nyliummc.essentials.api.modules.currency.modelhandlers.BalanceHandler
import net.minecraft.command.arguments.GameProfileArgumentType
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.LiteralText

object BalanceCommand {
    private val handler by lazy {
        EssentialsMod.instance!!.registry.getModelHandler(BalanceHandler::class.java)
    }

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        Command.builder(dispatcher) {
            command("bal", "balance", "money") {
                executes(::getBalance)

                argument("player", GameProfileArgumentType.gameProfile()) {
                    executes(::getTargetBalance)
                }

                argument("top") {
                    executes(::getBalanceTop)
                }
            }
        }
    }

    fun getBalanceTop(context: CommandContext<ServerCommandSource>): Int {
        val top = handler.getBalanceTop()
        val joined = top.joinToString("\n") { "${context.source.minecraftServer.userCache.getByUuid(it.uuid)}: ${it.balance}" }
        context.source.sendFeedback(LiteralText(joined), false)
        return 1
    }

    fun getBalance(context: CommandContext<ServerCommandSource>): Int {
        val player = context.source.player
        val stored = handler.getUser(player.uuid)
        context.source.sendFeedback(
                LiteralText("Balance: ${stored.balance}"),
                false)
        return 1
    }

    fun getTargetBalance(context: CommandContext<ServerCommandSource>): Int {
        val target = GameProfileArgumentType.getProfileArgument(context, "player").first()
        val stored = handler.getUser(target.id)
        context.source.sendFeedback(
                LiteralText("Balance for ${target.name}: ${stored.balance}"),
                false)
        return 1
    }
}