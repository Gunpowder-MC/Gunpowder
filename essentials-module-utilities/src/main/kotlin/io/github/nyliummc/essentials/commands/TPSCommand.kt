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
import io.github.nyliummc.essentials.EssentialsUtilitiesModule
import io.github.nyliummc.essentials.api.builders.Command
import io.github.nyliummc.essentials.api.builders.Text
import io.github.nyliummc.essentials.ext.precision
import net.minecraft.server.command.ServerCommandSource

object TPSCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        Command.builder(dispatcher) {
            command("tps") {
                executes(::execute)
            }
        }
    }

    fun execute(context: CommandContext<ServerCommandSource>): Int {
        val trackers = EssentialsUtilitiesModule.tpsTrackers

        var total = 0.0
        val count = trackers.size
        val width = trackers.map { (k, _) -> k.length }.toList().max()!! + 1

        context.source.player.sendMessage(Text.builder {
            text("${"Dimension".padEnd(width, ' ')} | TPS   | MSPT")
            trackers.forEach { id, tracker ->
                val tps = tracker.getTps()
                val mspt = tracker.getMspt()
                total += tps
                text("\n${id.padEnd(width, ' ')} | ${tps.precision(2).padEnd(5, ' ')} | ${mspt.precision(2)}")
            }
            text("\nOverall: ${(total / count).precision(2)}")
        }, false)
        return 1
    }
}
