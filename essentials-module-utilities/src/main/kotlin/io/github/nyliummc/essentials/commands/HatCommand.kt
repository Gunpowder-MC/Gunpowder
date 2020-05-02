package io.github.nyliummc.essentials.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import io.github.nyliummc.essentials.api.builders.Command
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.util.Hand

object HatCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        Command.builder(dispatcher) {
            command("hat") {
                executes(::execute)
            }
        }
    }

    private fun execute(context: CommandContext<ServerCommandSource>): Int {
        val player = context.source.player
        val hand = player.mainHandStack
        val head = player.inventory.armor[3]

        if (hand.isEmpty) {
            return 0
        }

        player.setStackInHand(Hand.MAIN_HAND, head)
        player.inventory.armor[3] = hand
        return 1
    }
}
