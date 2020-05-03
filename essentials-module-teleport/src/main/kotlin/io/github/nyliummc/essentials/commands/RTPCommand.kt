/*
 * MIT License
 *
 * Copyright (c) NyliumMC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.nyliummc.essentials.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import io.github.nyliummc.essentials.api.EssentialsMod
import io.github.nyliummc.essentials.api.builders.Command
import io.github.nyliummc.essentials.api.builders.TeleportRequest
import io.github.nyliummc.essentials.configs.TeleportConfig
import net.minecraft.block.Blocks
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.dimension.DimensionType

object RTPCommand {
    val config by lazy {
        EssentialsMod.instance.registry.getConfig(TeleportConfig::class.java)
    }

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        Command.builder(dispatcher) {
            command("rtp") {
                executes(::execute)
            }
        }
    }

    fun execute(context: CommandContext<ServerCommandSource>): Int {
        val distance = config.rtpDistance
        val player = context.source.player
        val world = context.source.minecraftServer.getWorld(DimensionType.OVERWORLD)
        val deltaX = distance * player.random.nextGaussian() + 1
        val deltaZ = distance * player.random.nextGaussian() + 1
        val newX = deltaX + player.x
        val newZ = deltaZ + player.z
        var newY = 0.0
        var i = world.seaLevel

        var pos = BlockPos(newX, i++.toDouble(), newZ)
        while ((!world.isAir(pos) || world.getBlockState(pos).block == Blocks.CAVE_AIR) && newY + world.seaLevel < world.height) {
            newY++
            pos = BlockPos(newX, i++.toDouble(), newZ)
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
        }.execute(config.teleportDelay.toLong())

        return 1
    }
}
