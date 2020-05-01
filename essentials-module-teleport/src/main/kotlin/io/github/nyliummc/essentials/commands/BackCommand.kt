package io.github.nyliummc.essentials.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import io.github.nyliummc.essentials.api.EssentialsMod
import io.github.nyliummc.essentials.api.builders.Command
import io.github.nyliummc.essentials.api.builders.TeleportRequest
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.LiteralText
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import net.minecraft.world.dimension.DimensionType
import java.util.*

object BackCommand {
    data class LastPosition(
            val position: Vec3d,
            val dimension: DimensionType,
            val facing: Vec2f
    )
    val lastPosition = mutableMapOf<UUID, LastPosition>()

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        Command.builder(dispatcher) {
            command("back") {
                executes(::execute)
            }
        }
    }

    private fun execute(context: CommandContext<ServerCommandSource>): Int {
        val id = context.source.player.uuid
        if (lastPosition.containsKey(id)) {
            val p = lastPosition[id]!!
            lastPosition.remove(lastPosition)

            context.source.sendFeedback(LiteralText("Teleporting to previous location"), false)

            val request = TeleportRequest.builder {
                player(context.source.player)
                destination(p.position)
                facing(p.facing)
                dimension(p.dimension)
            }

            request.execute(1)

            return 1
        }

        context.source.sendFeedback(LiteralText("No known last location"), false)
        return -1
    }
}
