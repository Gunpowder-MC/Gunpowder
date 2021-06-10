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
import io.github.gunpowder.api.builders.SidebarInfo
import io.github.gunpowder.mod.AbstractGunpowderMod
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.util.Formatting
import kotlin.concurrent.thread

object InfoCommand {
    private val welcomeFactory
        get() = SidebarInfo.factory {
            title("Gunpowder", Formatting.BOLD)
            line("Welcome to gunpowder!")
            line("")
            line("Modules loaded:")
            (GunpowderMod.instance as AbstractGunpowderMod).modules.forEach {
                line("- ${it.name}", Formatting.GREEN)
            }
        }

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        Command.builder(dispatcher) {
            command("info", "gunpowder", "/help") {
                executes(::showInfo)
            }
        }
    }

    private fun showInfo(commandContext: CommandContext<ServerCommandSource>): Int {
        val sidebar = welcomeFactory.invoke(commandContext.source.player)
        sidebar.removeAfter(5)

        return 1
    }
}
