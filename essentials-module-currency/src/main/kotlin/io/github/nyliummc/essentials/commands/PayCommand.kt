package io.github.nyliummc.essentials.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.context.CommandContext
import io.github.nyliummc.essentials.api.EssentialsMod
import io.github.nyliummc.essentials.api.builders.Command
import io.github.nyliummc.essentials.api.modules.currency.modelhandlers.BalanceHandler
import net.minecraft.command.arguments.GameProfileArgumentType
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.LiteralText
import kotlin.math.roundToInt

object PayCommand {
    val handler by lazy {
        EssentialsMod.instance!!.registry.getModelHandler(BalanceHandler::class.java)
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

        targetEntity?.addChatMessage(
                LiteralText("Received $$amount from ${from.name}"),
                false)
        return 1
    }
}