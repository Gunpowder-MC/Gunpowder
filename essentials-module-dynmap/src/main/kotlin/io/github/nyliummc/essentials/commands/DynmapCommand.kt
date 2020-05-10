package io.github.nyliummc.essentials.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import io.github.nyliummc.essentials.EssentialsDynmapModule
import io.github.nyliummc.essentials.api.builders.Command
import io.github.nyliummc.essentials.entities.FabricDynmapOnlinePlayer
import net.minecraft.server.command.ServerCommandSource

object DynmapCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        Command.builder(dispatcher) {
            command("dynmap") {
                // TODO sometime, a long time from now, in a galaxy far, far away: Implement full command tree

                literal("help") {
                    executes {
                        EssentialsDynmapModule.core.processCommand(FabricDynmapOnlinePlayer(it.source.player), "dynmap", "dynmap", arrayOf("help"))
                        return@executes 1
                    }
                }

                argument("args", StringArgumentType.greedyString()) {
                    executes(::runDynmapCommand)
                }
            }
        }
    }

    private fun runDynmapCommand(context: CommandContext<ServerCommandSource>): Int {
        val args = StringArgumentType.getString(context, "args").split(" ")

        EssentialsDynmapModule.core.processCommand(FabricDynmapOnlinePlayer(context.source.player), "dynmap", "dynmap", args.toTypedArray())

        return 1
    }
}
