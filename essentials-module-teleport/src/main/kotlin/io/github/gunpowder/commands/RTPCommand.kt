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

package io.github.gunpowder.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import io.github.gunpowder.api.GunpowderMod
import io.github.gunpowder.api.builders.Command
import io.github.gunpowder.api.builders.TeleportRequest
import io.github.gunpowder.configs.TeleportConfig
import net.minecraft.block.Blocks
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.LiteralText
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraft.world.dimension.DimensionType

object RTPCommand {
    val config by lazy {
        GunpowderMod.instance.registry.getConfig(TeleportConfig::class.java)
    }

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        Command.builder(dispatcher) {
            command("rtp") {
                executes(RTPCommand::execute)
            }
        }
    }

    fun execute(context: CommandContext<ServerCommandSource>): Int {
        val distance = config.rtpDistance
        val player = context.source.player
        val world = context.source.minecraftServer.getWorld(World.OVERWORLD)!!
        val deltaX = distance * player.random.nextGaussian() + 1
        val deltaZ = distance * player.random.nextGaussian() + 1
        val newX = deltaX + player.x
        val newZ = deltaZ + player.z
        var newY = 0.0
        var i = world.seaLevel

        if (world.getBlockState(BlockPos(newX, world.seaLevel.toDouble(), newZ)).block == Blocks.VOID_AIR) {
            // Void world, cancel teleport
            player.sendMessage(LiteralText("Void world detected, cancelling..."), false)
            return -1
        }

        if (world.isWater(BlockPos(newX, world.seaLevel.toDouble(), newZ))) {
            return execute(context)
        }

        while (world.getBlockState(BlockPos(newX, i++.toDouble(), newZ)).block != Blocks.AIR && newY + world.seaLevel < world.height) {
            newY++
        }

        TeleportRequest.builder {
            player(player)
            destination(Vec3d(newX, i.toDouble(), newZ))
            dimension(DimensionType.OVERWORLD_REGISTRY_KEY.value)
            facing(player.rotationClient)
        }.execute(config.teleportDelay.toLong())

        if (config.teleportDelay > 0) {
            context.source.sendFeedback(LiteralText("Teleporting in ${config.teleportDelay.toLong()} seconds..."), false)
        }

        return 1
    }
}
