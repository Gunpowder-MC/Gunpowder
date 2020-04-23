package io.github.nyliummc.essentials.api.builders

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.github.nyliummc.essentials.api.EssentialsMod
import net.minecraft.server.command.ServerCommandSource
import java.util.concurrent.CompletableFuture

interface Command {
    companion object fun builder(dispatcher: CommandDispatcher<ServerCommandSource>, callback: Builder.() -> Unit) {
        val builder = EssentialsMod.instance!!.registry.getBuilder(Builder::class.java)
        builder.setDispatcher(dispatcher)
        callback(builder)
    }

    interface Builder {
        fun command(vararg names: String, builder: () -> Unit)
        fun requires(checkFunction: (ServerCommandSource) -> Boolean)
        fun suggests(callback: (CommandContext<ServerCommandSource>, SuggestionsBuilder) -> CompletableFuture<Suggestions>)
        fun argument(name: String, type: ArgumentType<*>, builder: () -> Unit)
        fun argument(literal: String, builder: () -> Unit)
        fun executes(callback: (CommandContext<ServerCommandSource>) -> Int)

        @Deprecated("Used internally, do not use.")
        fun setDispatcher(dispatcher: CommandDispatcher<ServerCommandSource>)
    }
}
