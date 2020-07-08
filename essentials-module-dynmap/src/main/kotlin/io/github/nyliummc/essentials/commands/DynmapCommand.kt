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
