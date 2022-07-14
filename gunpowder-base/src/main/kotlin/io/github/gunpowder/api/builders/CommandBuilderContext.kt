package io.github.gunpowder.api.builders

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.github.gunpowder.api.GunpowderModule
import net.minecraft.server.command.ServerCommandSource
import java.util.concurrent.CompletableFuture

interface CommandBuilderContext {
    context(GunpowderModule)
    fun command(vararg names: String, dedicatedOnly: Boolean = false, block: CommandBuilder.() -> Unit)

    interface CommandBuilder {
        fun literal(vararg literals: String, block: CommandBuilder.() -> Unit)
        fun requires(block: (ServerCommandSource) -> Boolean)
        fun executes(block: CommandContext<ServerCommandSource>.() -> Int)
        fun <T> argumentImpl(name: String, type: ArgumentType<T>, clazz: Class<T>, block: ArgumentCommandBuilder.(CommandContext<ServerCommandSource>.() -> T) -> Unit)
        fun <T> optArgumentImpl(name: String, type: ArgumentType<T>, clazz: Class<T>, block: ArgumentCommandBuilder.(CommandContext<ServerCommandSource>.() -> T?) -> Unit)
    }

    interface ArgumentCommandBuilder : CommandBuilder {
        fun suggests(block: CommandContext<ServerCommandSource>.(SuggestionsBuilder) -> CompletableFuture<Suggestions>)
    }
}

inline fun <reified T> CommandBuilderContext.CommandBuilder.argument(name: String, type: ArgumentType<T>, noinline block: CommandBuilderContext.ArgumentCommandBuilder.(CommandContext<ServerCommandSource>.() -> T) -> Unit) = argumentImpl(name, type, T::class.java, block)
inline fun <reified T> CommandBuilderContext.CommandBuilder.optArgument(name: String, type: ArgumentType<T>, noinline block: CommandBuilderContext.ArgumentCommandBuilder.(CommandContext<ServerCommandSource>.() -> T?) -> Unit) = optArgumentImpl(name, type, T::class.java, block)
