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