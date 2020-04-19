package io.github.nyliummc.essentials.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import io.github.nyliummc.essentials.createCommands
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.LiteralText

object InfoCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        createCommands(dispatcher) {
            command("essentials") {
                executes(::showInfo)
            }
        }
    }

    private fun showInfo(commandContext: CommandContext<ServerCommandSource>): Int {
        commandContext.source.player.addChatMessage(LiteralText("Welcome to essentials! A better message coming soon."), false)
        return 1
    }
}
