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

package io.github.nyliummc.essentials.api.builders

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.github.nyliummc.essentials.api.EssentialsMod
import net.minecraft.server.command.ServerCommandSource
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

interface Command {
    companion object {
        /**
         * Command creation DSL, see {@link io.github.nyliummc.essentials.api.builders.Command.Builder}
         */
        @JvmStatic
        fun builder(dispatcher: CommandDispatcher<ServerCommandSource>, callback: Consumer<Builder>) = builder(dispatcher, callback::accept)
        fun builder(dispatcher: CommandDispatcher<ServerCommandSource>, callback: Builder.() -> Unit) {
            val builder = EssentialsMod.instance.registry.getBuilder(Builder::class.java)
            builder.setDispatcher(dispatcher)
            callback(builder)
        }
    }

    interface Builder {
        /**
         * The name of the command with aliases.
         * Starts a block of {@link io.github.nyliummc.essentials.api.builders.Command.CommandBuilder}.
         */
        fun command(vararg names: String, builder: Consumer<CommandBuilder>) = command(*names, builder=builder::accept)
        fun command(vararg names: String, builder: CommandBuilder.() -> Unit)

        @Deprecated("Used internally, do not use.")
        fun setDispatcher(dispatcher: CommandDispatcher<ServerCommandSource>)
    }

    interface CommandBuilder {
        /**
         * Set requirements for running this function.
         */
        fun requires(checkFunction: (ServerCommandSource) -> Boolean)

        /**
         * Argument parameter, see {@link io.github.nyliummc.essentials.api.builders.Command.ArgumentBuilder}.
         */
        fun argument(name: String, type: ArgumentType<*>, builder: Consumer<ArgumentBuilder>) = argument(name, type, builder::accept)
        fun argument(name: String, type: ArgumentType<*>, builder: ArgumentBuilder.() -> Unit)

        /**
         * Literal argument.
         */
        fun literal(vararg literals: String, builder: Consumer<ArgumentBuilder>) = literal(*literals, builder=builder::accept)
        fun literal(vararg literals: String, builder: ArgumentBuilder.() -> Unit)

        /**
         * Code or function to execute on this command.
         */
        fun executes(callback: (CommandContext<ServerCommandSource>) -> Int)
    }

    interface ArgumentBuilder : CommandBuilder {
        /**
         * Suggestion provider for this callback.
         */
        fun suggests(callback: (CommandContext<ServerCommandSource>, SuggestionsBuilder) -> CompletableFuture<Suggestions>)
    }
}
