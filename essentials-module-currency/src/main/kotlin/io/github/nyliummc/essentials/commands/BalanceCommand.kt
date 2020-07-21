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
import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.context.CommandContext
import io.github.nyliummc.essentials.api.EssentialsMod
import io.github.nyliummc.essentials.api.builders.Command
import io.github.nyliummc.essentials.api.module.currency.modelhandlers.BalanceHandler
import net.minecraft.command.arguments.GameProfileArgumentType
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.LiteralText
import kotlin.math.roundToInt

object BalanceCommand {
    private val handler by lazy {
        EssentialsMod.instance.registry.getModelHandler(BalanceHandler::class.java)
    }

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        Command.builder(dispatcher) {
            command("bal", "balance", "money") {
                executes(::getBalance)

                argument("player", GameProfileArgumentType.gameProfile()) {
                    executes(::getTargetBalance)

                    literal("set") {
                        requires { it.hasPermissionLevel(4) }

                        argument("amount", FloatArgumentType.floatArg(0.0F)) {
                            executes(::adminSet)
                        }
                    }

                    literal("add") {
                        requires { it.hasPermissionLevel(4) }

                        argument("amount", FloatArgumentType.floatArg(0.0F)) {
                            executes(::adminAdd)
                        }
                    }

                    literal("remove") {
                        requires { it.hasPermissionLevel(4) }

                        argument("amount", FloatArgumentType.floatArg(0.0F)) {
                            executes(::adminRemove)
                        }
                    }
                }

                literal("top") {
                    executes(::getBalanceTop)
                }
            }
        }
    }

    fun adminSet(context: CommandContext<ServerCommandSource>): Int {
        val amount = (FloatArgumentType.getFloat(context, "amount") * 100).roundToInt() / 100.0 // only 2 decimal places
        val player = GameProfileArgumentType.getProfileArgument(context, "player").first()

        handler.modifyUser(player.id) {
            it.balance = amount.toBigDecimal()
            it
        }

        context.source.sendFeedback(
                LiteralText("Set balance of player ${player.name} to $amount"),
                false)

        return 1
    }

    fun adminAdd(context: CommandContext<ServerCommandSource>): Int {
        val amount = (FloatArgumentType.getFloat(context, "amount") * 100).roundToInt() / 100.0 // only 2 decimal places
        val player = GameProfileArgumentType.getProfileArgument(context, "player").first()

        handler.modifyUser(player.id) {
            it.balance.add(amount.toBigDecimal())
            it
        }

        context.source.sendFeedback(
                LiteralText("Added $amount to balance of player ${player.name}"),
                false)

        return 1
    }

    fun adminRemove(context: CommandContext<ServerCommandSource>): Int {
        val amount = (FloatArgumentType.getFloat(context, "amount") * 100).roundToInt() / 100.0 // only 2 decimal places
        val player = GameProfileArgumentType.getProfileArgument(context, "player").first()

        handler.modifyUser(player.id) {
            it.balance.add(amount.toBigDecimal())
            if (it.balance < (0).toBigDecimal()) {
                it.balance = (0).toBigDecimal();
            }
            it
        }

        context.source.sendFeedback(
                LiteralText("Added $amount to balance of player ${player.name}"),
                false)

        return 1
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
