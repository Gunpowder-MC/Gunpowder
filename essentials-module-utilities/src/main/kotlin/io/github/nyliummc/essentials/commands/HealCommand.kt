package io.github.nyliummc.essentials.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import io.github.nyliummc.essentials.api.builders.Command
import net.minecraft.command.arguments.EntityArgumentType
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.LiteralText

object HealCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        Command.builder(dispatcher) {
            command("heal") {
                requires { it.hasPermissionLevel(4) }
                executes(::healSelf)

                argument("player", EntityArgumentType.player()) {
                    requires { it.hasPermissionLevel(4) }
                    executes(::healOther)
                }
            }
        }
    }

    private fun healSelf(context: CommandContext<ServerCommandSource>): Int {
        healPlayer(context.source.player)
        context.source.sendFeedback(
                LiteralText("Successfully healed"),
                false)
        return 1
    }

    private fun healOther(context: CommandContext<ServerCommandSource>): Int {
        val player = EntityArgumentType.getPlayer(context, "player")
        healPlayer(player)
        context.source.sendFeedback(
                LiteralText("Successfully healed ${player.displayName.asString()}"),
                false)
        return 1
    }

    private fun healPlayer(player: ServerPlayerEntity) {
        player.health = player.maximumHealth;
        player.hungerManager.foodLevel = 20;
        player.extinguish();
        player.clearStatusEffects();
    }
}
