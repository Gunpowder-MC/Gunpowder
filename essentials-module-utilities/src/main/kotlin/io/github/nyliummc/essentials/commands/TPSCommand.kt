package io.github.nyliummc.essentials.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import io.github.nyliummc.essentials.EssentialsUtilitiesModule
import io.github.nyliummc.essentials.api.builders.Command
import io.github.nyliummc.essentials.api.builders.Text
import io.github.nyliummc.essentials.ext.precision
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.LiteralText

object TPSCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        Command.builder(dispatcher) {
            command("tps") {
                executes(::execute)
            }
        }
    }

    fun execute(context: CommandContext<ServerCommandSource>): Int {
        val trackers = EssentialsUtilitiesModule.tpsTrackers

        var total = 0.0
        val count = trackers.size
        val width = trackers.map { (k, _) -> k.length }.toList().max()!! + 1

        context.source.player.addChatMessage(Text.builder {
            text("${"Dimension".padEnd(width, ' ')} | TPS   | MSPT")
            trackers.forEach { id, tracker ->
                val tps = tracker.getTps()
                val mspt = tracker.getMspt()
                total += tps
                text("\n${id.padEnd(width, ' ')} | ${tps.precision(2).padEnd(5, ' ')} | ${mspt.precision(2)}")
            }
            text("\nOverall: ${(total / count).precision(2)}")
        }, false)
        return 1
    }
}
