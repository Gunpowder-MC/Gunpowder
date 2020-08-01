package io.github.nyliummc.essentials.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import io.github.nyliummc.essentials.api.builders.Command
import net.minecraft.network.MessageType
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.LiteralText
import net.minecraft.util.Util

object PingCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        Command.builder(dispatcher) {
            command("ping") {
                executes(::execute)
            }
        }
    }

    private fun execute(context: CommandContext<ServerCommandSource>): Int {
        context.source.player.sendMessage(LiteralText("Ping: ${context.source.player.pingMilliseconds}ms"), MessageType.CHAT, Util.NIL_UUID)
        return 1
    }
}
