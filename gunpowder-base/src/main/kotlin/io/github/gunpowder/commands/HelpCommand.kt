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

import com.google.common.collect.Iterables
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import io.github.gunpowder.api.builders.Command
import io.github.gunpowder.api.builders.Text
import net.minecraft.server.command.HelpCommand
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.LiteralText
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting

object HelpCommand {
    private lateinit var dispatcher: CommandDispatcher<ServerCommandSource>
    val ITEMS_PER_PAGE = 12

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        this.dispatcher = dispatcher

        Command.builder(dispatcher) {
            command("help") {
                permission("minecraft.help", 0)

                executes(::helpRoot)

                argument("page", IntegerArgumentType.integer(1)) {
                    permission("minecraft.help.page", 0)
                    executes(::helpPage)
                }

                argument("command", StringArgumentType.greedyString()) {
                    permission("minecraft.help.command", 0)
                    executes(::helpCommand)
                }
            }
        }
    }

    private fun helpRoot(context: CommandContext<ServerCommandSource>): Int {
        return helpPage(context, 1)
    }

    private fun helpPage(context: CommandContext<ServerCommandSource>): Int {
        return helpPage(context, IntegerArgumentType.getInteger(context, "page"))
    }

    private fun helpPage(context: CommandContext<ServerCommandSource>, page: Int): Int {
        val items = dispatcher.getSmartUsage(
            dispatcher.root,
            context.source
        ).toList()
        val maxPage = items.size / ITEMS_PER_PAGE
        if (page > maxPage+1) {
            context.source.sendError(LiteralText("Invalid page"))
            return -1
        }
        val entries = items.subList((page-1) * ITEMS_PER_PAGE, (page * ITEMS_PER_PAGE).coerceAtMost(items.size))
        context.source.sendFeedback(Text.builder {
            text("\n".repeat(ITEMS_PER_PAGE - entries.size + 6))
            text("========== Help --- Page $page/${maxPage+1} ==========\n") {
                color(Formatting.YELLOW)
            }
            entries.forEach {
                text("/${it.second}\n") {
                    color(Formatting.GREEN)
                }
            }
            text("========== ") { color(Formatting.YELLOW) }
            text("Previous") {
                if (page == 1) {
                    color(Formatting.GRAY)
                } else {
                    color(Formatting.BLUE)
                    onClickCommand("/help ${page - 1}")
                }
            }
            text(" / ") { color(Formatting.YELLOW) }
            text("Next") {
                if (page == maxPage+1) {
                    color(Formatting.GRAY)
                } else {
                    color(Formatting.BLUE)
                    onClickCommand("/help ${page + 1}")
                }
            }
            text(" ==========") { color(Formatting.YELLOW) }
        }, false)
        return entries.size
    }

    private fun helpCommand(context: CommandContext<ServerCommandSource>): Int {
        // Vanilla code

        val parseResults = dispatcher.parse(
            StringArgumentType.getString(context, "command"),
            context.source
        )
        if (parseResults.context.nodes.isEmpty()) {
            throw SimpleCommandExceptionType(TranslatableText("commands.help.failed")).create()
        } else {
            val map = dispatcher.getSmartUsage(
                (Iterables.getLast(
                    parseResults.context.nodes
                )).node,
                context.source as ServerCommandSource
            ).onEach {
                context.source.sendFeedback(LiteralText("/${parseResults.reader.string} ${it.value}"), false)
            }
            return map.size
        }
    }
}
