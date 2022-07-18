package io.github.gunpowder.builders

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.github.gunpowder.api.GunpowderModule
import io.github.gunpowder.api.builders.CommandBuilderContext
import io.github.gunpowder.api.ext.hasPermission
import io.github.gunpowder.api.types.Command
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
import net.minecraft.server.command.ServerCommandSource
import java.util.WeakHashMap
import java.util.concurrent.CompletableFuture

object CommandBuilderContextProviderImpl : GunpowderModule.CommandBuilderContextProvider {
    override fun get(module: GunpowderModule): CommandBuilderContext {
        return CommandBuilderContextImpl(module)
    }
}


class CommandBuilderContextImpl(private val module: GunpowderModule) : CommandBuilderContext {
    override fun command(vararg names: String, dedicatedOnly: Boolean, block: CommandBuilderContext.CommandBuilder.() -> Unit) {
        CommandRegistrationCallback.EVENT.register { dispatcher, isDedicated ->
            if (dedicatedOnly && !isDedicated) return@register

            for (name in names) {
                val node = LiteralArgumentBuilder.literal<ServerCommandSource>(name)
                val builder = CommandBuilderImpl(node)
                builder.block()
                dispatcher.register(node)
            }
        }
    }

    override fun load(command: Command) {
        with(command) {
            build()
        }
    }

    private open inner class CommandBuilderImpl(protected val root: ArgumentBuilder<ServerCommandSource, *>) : CommandBuilderContext.CommandBuilder {
        init {
            root.requires { module.enabled }
        }

        override fun literal(vararg literals: String, block: CommandBuilderContext.CommandBuilder.() -> Unit) {
            for (literal in literals) {
                val node = LiteralArgumentBuilder.literal<ServerCommandSource>(literal)
                val builder = CommandBuilderImpl(node)
                builder.block()
                root.then(node)
            }
        }

        override fun <T> argumentImpl(name: String, type: ArgumentType<T>, clazz: Class<T>, block: CommandBuilderContext.ArgumentCommandBuilder.(CommandContext<ServerCommandSource>.() -> T) -> Unit) {
            val node = RequiredArgumentBuilder.argument<ServerCommandSource, T>(name, type)
            val builder = ArgumentCommandBuilderImpl(node)
            val map = WeakHashMap<CommandContext<ServerCommandSource>, T>()
            builder.block {
                map.getOrPut(this) {
                    getArgument(name, clazz)
                }
            }
            root.then(node)
        }

        override fun <T> optArgumentImpl(
            name: String,
            type: ArgumentType<T>,
            clazz: Class<T>,
            block: CommandBuilderContext.ArgumentCommandBuilder.(CommandContext<ServerCommandSource>.() -> T?) -> Unit
        ) {
            argumentImpl(name, type, clazz, block)
            val rootBuilder = ArgumentCommandBuilderImpl(root, false)
            rootBuilder.block {
                null
            }
        }

        override fun requires(block: (ServerCommandSource) -> Boolean) {
            root.requires {
                module.enabled && block(it)
            }
        }

        override fun permission(permission: String, default: Boolean) {
            root.requires {
                module.enabled && it.hasPermission(permission, default)
            }
        }

        override fun permission(permission: String, permissionLevel: Int) {
            root.requires {
                module.enabled && it.hasPermission(permission, permissionLevel)
            }
        }

        override fun executes(block: CommandContext<ServerCommandSource>.() -> Int) {
            root.executes(block)
        }
    }

    private inner class ArgumentCommandBuilderImpl(private val root2: ArgumentBuilder<ServerCommandSource, *>, private val isArgument: Boolean = true) : CommandBuilderImpl(root2), CommandBuilderContext.ArgumentCommandBuilder {
        override fun suggests(block: CommandContext<ServerCommandSource>.(SuggestionsBuilder) -> CompletableFuture<Suggestions>) {
            if (isArgument) {
                (root2 as RequiredArgumentBuilder<ServerCommandSource, *>).suggests(block)
            }
        }

        override fun requires(block: (ServerCommandSource) -> Boolean) {
            if (isArgument) super.requires(block)
        }

        override fun permission(permission: String, default: Boolean) {
            if (isArgument) super.permission(permission, default)
        }

        override fun permission(permission: String, permissionLevel: Int) {
            if (isArgument) super.permission(permission, permissionLevel)
        }
    }
}
