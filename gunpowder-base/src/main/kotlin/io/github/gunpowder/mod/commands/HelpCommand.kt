package io.github.gunpowder.mod.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import io.github.gunpowder.api.GunpowderModule
import io.github.gunpowder.api.types.Command
import io.github.gunpowder.api.builders.CommandBuilderContext
import io.github.gunpowder.api.builders.argument
import io.github.gunpowder.api.builders.optArgument
import io.github.gunpowder.api.ext.error
import io.github.gunpowder.api.ext.feedback
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.HoverEvent
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting

object HelpCommand : Command {
    lateinit var dispatcher: CommandDispatcher<ServerCommandSource>
    private const val ITEMS_PER_PAGE = 12

    context(GunpowderModule, CommandBuilderContext)
    override fun build() {
        command("help") {
            optArgument("page", IntegerArgumentType.integer(1)) { page ->
                executes {
                    helpPage(page() ?: 1)
                }
            }

            argument("command", StringArgumentType.greedyString()) { command ->
                executes {
                    helpCommand(command())
                }
            }
        }
    }

    private fun CommandContext<ServerCommandSource>.helpPage(page: Int) : Int {
        val items = dispatcher.getSmartUsage(dispatcher.root, source).toList()
        val maxPage = items.size / ITEMS_PER_PAGE
        if (page > maxPage+1) {
            this.error { text("Invalid page") }
            return -1
        }
        val entries = items.subList((page-1) * ITEMS_PER_PAGE, (page * ITEMS_PER_PAGE).coerceAtMost(items.size))

        this.feedback {
            text("\n".repeat(ITEMS_PER_PAGE - entries.size + 6))
            text("========== Help --- Page $page/${maxPage+1} ==========\n") {
                formatting(Formatting.YELLOW)
            }
            entries.forEach {
                text("/${it.second}\n") {
                    formatting(Formatting.GREEN)
                }
            }
            text("========== ") { formatting(Formatting.YELLOW) }
            text("Previous") {
                if (page == 1) {
                    formatting(Formatting.GRAY)
                } else {
                    formatting(Formatting.BLUE)
                    onClickCommand("/help ${page - 1}")
                    hoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, TranslatableText("Previous page")))
                }
            }
            text(" / ") { formatting(Formatting.YELLOW) }
            text("Next") {
                if (page == maxPage+1) {
                    formatting(Formatting.GRAY)
                } else {
                    formatting(Formatting.BLUE)
                    onClickCommand("/help ${page + 1}")
                    hoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, TranslatableText("Next page")))
                }
            }
            text(" ==========") { formatting(Formatting.YELLOW) }
        }
        return entries.size
    }

    private fun CommandContext<ServerCommandSource>.helpCommand(command: String) : Int {
        val parseResults = dispatcher.parse(command, source)
        if (parseResults.context.nodes.isEmpty()) {
            throw SimpleCommandExceptionType(TranslatableText("commands.help.failed")).create()
        } else {
            val map = dispatcher.getSmartUsage(parseResults.context.nodes.last().node, source)
            this.feedback {
                map.onEachIndexed { index, entry ->
                    text("/${parseResults.reader.string} ${entry.value}${if (index == map.size-1) "" else "\n"}")
                }
            }
            return map.size
        }
    }
}
