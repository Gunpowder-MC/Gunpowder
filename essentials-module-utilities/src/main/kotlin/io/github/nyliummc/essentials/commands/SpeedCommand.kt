package io.github.nyliummc.essentials.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import io.github.ladysnake.pal.Pal
import io.github.ladysnake.pal.VanillaAbilities
import io.github.nyliummc.essentials.api.builders.Command
import net.minecraft.command.arguments.EntityArgumentType
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.LiteralText

object SpeedCommand {
    val ESSENTIALS_ABILITY_SPEED = Pal.getAbilitySource("essentials", "speed");

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        Command.builder(dispatcher) {
            command("speed") {
                requires { it.hasPermissionLevel(4) }
                executes(::clearSpeedSelf)

                argument("player", EntityArgumentType.player()) {
                    requires { it.hasPermissionLevel(4) }

                    executes(::clearSpeedOther)
                }

                argument("speed", DoubleArgumentType.doubleArg(0.0, 10.0)) {
                    requires { it.hasPermissionLevel(4) }
                    executes(::setSpeedSelf)

                    argument("player", EntityArgumentType.player()) {
                        requires { it.hasPermissionLevel(4) }

                        executes(::setSpeedOther)
                    }
                }
            }
        }
    }

    private fun clearSpeedSelf(context: CommandContext<ServerCommandSource>): Int {
        setSpeed(context.source.player, 0.05)

        // Send feedback
        context.source.sendFeedback(
                LiteralText("Successfully reset speed"),
                false)

        return 1
    }

    private fun clearSpeedOther(context: CommandContext<ServerCommandSource>): Int {
        val player = EntityArgumentType.getPlayer(context, "player")
        setSpeed(player, 0.05)

        // Send feedback
        context.source.sendFeedback(
                LiteralText("Successfully reset speed for ${player.displayName.asString()}"),
                false)

        return 1
    }

    private fun setSpeedSelf(context: CommandContext<ServerCommandSource>): Int {
        val speed = DoubleArgumentType.getDouble(context, "speed")
        setSpeed(context.source.player, speed)

        // Send feedback
        context.source.sendFeedback(
                LiteralText("Successfully set speed to $speed"),
                false)

        return 1
    }

    private fun setSpeedOther(context: CommandContext<ServerCommandSource>): Int {
        val player = EntityArgumentType.getPlayer(context, "player")
        val speed = DoubleArgumentType.getDouble(context, "speed")
        setSpeed(player, speed)

        // Send feedback
        context.source.sendFeedback(
                LiteralText("Successfully set speed for ${player.displayName.asString()} to $speed"),
                false)

        return 1
    }

    private fun setSpeed(player: ServerPlayerEntity, speed: Double) {
        // TODO: Use PAL once it supports this
        player.abilities.flySpeed = speed.toFloat()
        player.abilities.walkSpeed = 2*speed.toFloat()
        player.sendAbilitiesUpdate()
    }
}
