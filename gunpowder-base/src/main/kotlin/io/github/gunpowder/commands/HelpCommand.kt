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
                executes(::helpRoot)

                argument("page", IntegerArgumentType.integer(1)) {
                    executes(::helpPage)
                }

                argument("command", StringArgumentType.greedyString()) {
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
