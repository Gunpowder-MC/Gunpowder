/*
 * MIT License
 *
 * Copyright (c) GunpowderMC
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
import io.github.gunpowder.api.builders.Text
import io.github.gunpowder.mod.AbstractGunpowderMod
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.util.Formatting

object InfoCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        Command.builder(dispatcher) {
            command("info", "gunpowder") {
                executes(::showInfo)
            }
        }
    }

    private fun showInfo(commandContext: CommandContext<ServerCommandSource>): Int {
        commandContext.source.sendFeedback(
                Text.builder {
                    text("Welcome to Gunpowder!")
                    text("\nModules loaded:")
                    text("\n- ")
                    text("base") {
                        color(Formatting.GOLD)
                    }
                    (GunpowderMod.instance as AbstractGunpowderMod).modules.forEach {
                        text("\n- ")
                        text(it.name) {
                            color(Formatting.GOLD)
                        }
                    }
                },
                false)
        return 1
    }
}
