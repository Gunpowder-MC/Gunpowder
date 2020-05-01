package io.github.nyliummc.essentials.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import io.github.nyliummc.essentials.api.builders.Command
import io.github.nyliummc.essentials.api.builders.TeleportRequest
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import net.minecraft.world.dimension.DimensionType

object SpawnCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        Command.builder(dispatcher) {
            command("spawn") {
                executes(::execute)
            }
        }
    }

    fun execute(context: CommandContext<ServerCommandSource>): Int {
        val player = context.source.player
        val props = context.source.world.levelProperties

        TeleportRequest.builder {
            player(player)
            dimension(DimensionType.OVERWORLD)
            destination(Vec3d(props.spawnX.toDouble(), props.spawnY.toDouble(), props.spawnZ.toDouble()))
        }.execute(0)

        return 1
    }
}
