package io.github.gunpowder.api.builders

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.github.gunpowder.api.types.Command
import net.minecraft.server.command.ServerCommandSource
import java.util.concurrent.CompletableFuture

interface CommandBuilderContext {
    fun command(vararg names: String, dedicatedOnly: Boolean = false, block: CommandBuilder.() -> Unit)
    fun load(command: Command)

    interface CommandBuilder {
        fun literal(vararg literals: String, block: CommandBuilder.() -> Unit)

        /**
         * Permissions are not compatible with requires, so if you need both a check and permissions,
         * use hasPermission in requires instead.
         *
         * in optArgument() blocks, requires and permission calls only apply themselves to
         * cases where the argument is present.
         */
        fun requires(block: (ServerCommandSource) -> Boolean)
        fun permission(permission: String, default: Boolean = false)
        fun permission(permission: String, permissionLevel: Int)
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
