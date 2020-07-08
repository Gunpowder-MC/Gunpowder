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

object PayCommand {
    val handler by lazy {
        EssentialsMod.instance.registry.getModelHandler(BalanceHandler::class.java)
    }

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        Command.builder(dispatcher) {
            command("pay") {
                argument("player", GameProfileArgumentType.gameProfile()) {
                    argument("amount", FloatArgumentType.floatArg(0.0F)) {
                        executes(::execute)
                    }
                }
            }
        }
    }

    fun execute(context: CommandContext<ServerCommandSource>): Int {
        val from = context.source.player
        val to = GameProfileArgumentType.getProfileArgument(context, "player").first()
        val amount = (FloatArgumentType.getFloat(context, "amount") * 100).roundToInt() / 100.0 // only 2 decimal places
        val userFrom = handler.getUser(from.uuid)
        if (userFrom.balance < amount.toBigDecimal() || amount <= 0) {
            context.source.sendFeedback(
                    LiteralText("Unable to pay $$amount"),
                    false)
            return -1
        }

        userFrom.balance -= amount.toBigDecimal()
        handler.updateUser(userFrom)

        handler.modifyUser(to.id) {
            it.balance += amount.toBigDecimal()
            it
        }

        context.source.sendFeedback(
                LiteralText("Paid $$amount to ${to.name}"),
                false)

        val targetEntity = context.source.minecraftServer.playerManager.getPlayer(to.id)

        targetEntity?.sendMessage(
                LiteralText("Received $$amount from ${from.entityName}"),
                false)
        return 1
    }
}
