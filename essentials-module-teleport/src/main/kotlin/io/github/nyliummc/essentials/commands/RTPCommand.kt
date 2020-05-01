package io.github.nyliummc.essentials.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import io.github.nyliummc.essentials.api.builders.Command
import io.github.nyliummc.essentials.api.builders.TeleportRequest
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import net.minecraft.world.dimension.DimensionType

object RTPCommand {
    val rtpDistance = 10000

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        Command.builder(dispatcher) {
            command("rtp") {
                executes(::execute)
            }
        }
    }

    fun execute(context: CommandContext<ServerCommandSource>): Int {
        val distance = rtpDistance
        val player = context.source.player
        val world = context.source.minecraftServer.getWorld(DimensionType.OVERWORLD)
        val deltaX = distance * player.random.nextGaussian() + 1
        val deltaZ = distance * player.random.nextGaussian() + 1
        val newX = deltaX + player.x
        val newZ = deltaZ + player.z
        var newY = 0.0
        var i = world.seaLevel

        while (!world.isAir(BlockPos(newX, i++.toDouble(), newZ)) && newY + world.seaLevel < world.height) {
            newY++
        }

        if (world.isWater(BlockPos(newX, (i - 2).toDouble(), newZ))) {
            return execute(context)
        }

        BackCommand.lastPosition[player.uuid] = BackCommand.LastPosition(player.pos, player.dimension, player.rotationClient)

        TeleportRequest.builder {
            player(player)
            destination(Vec3d(newX, i.toDouble(), newZ))
            dimension(DimensionType.OVERWORLD)
            facing(player.rotationClient)
        }.execute(0)

        return 1
    }
}
