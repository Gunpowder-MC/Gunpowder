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

package io.github.nyliummc.essentials.entities.builders

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import java.util.concurrent.CompletableFuture
import com.mojang.brigadier.builder.ArgumentBuilder as BrigadierArgumentBuilder
import io.github.nyliummc.essentials.api.builders.Command as APICommand

object Command : APICommand {
    class Builder : APICommand.Builder {
        private lateinit var dispatcher: CommandDispatcher<ServerCommandSource>

        override fun command(vararg names: String, builder: APICommand.CommandBuilder.() -> Unit) {
            val base = names.first()
            val aliases = names.filter { it != base }

            names.forEach {
                val baseCommand = CommandManager.literal(it)
                builder(CommandBuilder(baseCommand))
                val baseNode = dispatcher.register(baseCommand)
            }

            /*
            TODO: Use this instead of building for every alias once
                  https://github.com/Mojang/brigadier/issues/46 is fixed
            aliases.forEach {
                val node = CommandManager.literal(it)
                node.fork(baseNode)
                node.executes(baseNode.command)
                dispatcher.register(node)
            }
            */
        }

        override fun setDispatcher(dispatcher: CommandDispatcher<ServerCommandSource>) {
            this.dispatcher = dispatcher
        }
    }

    open class CommandBuilder(internal val command: BrigadierArgumentBuilder<ServerCommandSource, *>) : APICommand.CommandBuilder {
        override fun requires(checkFunction: (ServerCommandSource) -> Boolean) {
            this.command.requires(checkFunction)
        }

        private fun argumentNode(node: BrigadierArgumentBuilder<ServerCommandSource, *>, builder: APICommand.ArgumentBuilder.() -> Unit) {
            builder(ArgumentBuilder(node))
            command.then(node)
        }

        override fun argument(name: String, type: ArgumentType<*>, builder: APICommand.ArgumentBuilder.() -> Unit) {
            val arg = CommandManager.argument(name, type)
            argumentNode(arg, builder)
        }

        override fun literal(vararg literals: String, builder: APICommand.ArgumentBuilder.() -> Unit) {
            literals.forEach {
                val arg = CommandManager.literal(it)
                argumentNode(arg, builder)
            }
            // TODO: switch to redirects if possible
        }

        override fun executes(callback: (CommandContext<ServerCommandSource>) -> Int) {
            command.executes(callback)
        }
    }

    class ArgumentBuilder(argument: BrigadierArgumentBuilder<ServerCommandSource, *>) : CommandBuilder(argument), APICommand.ArgumentBuilder {
        override fun suggests(callback: (CommandContext<ServerCommandSource>, SuggestionsBuilder) -> CompletableFuture<Suggestions>) {
            (this.command as RequiredArgumentBuilder<ServerCommandSource, *>).suggests(callback)
        }
    }
}
