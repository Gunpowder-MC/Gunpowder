package io.github.nyliummc.essentials.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import io.github.nyliummc.essentials.api.builders.Command
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.LiteralText

object InfoCommand {
    fun register(builder: Command, dispatcher: CommandDispatcher<ServerCommandSource>) {
        builder.builder(dispatcher) {
            command("info") {
                executes(::showInfo)
            }
        }
    }

    private fun showInfo(commandContext: CommandContext<ServerCommandSource>): Int {
        commandContext.source.player.addChatMessage(LiteralText("Welcome to essentials! A better message coming soon."), false)
        return 1
    }
}
