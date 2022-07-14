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
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
import net.minecraft.server.command.ServerCommandSource
import java.util.concurrent.CompletableFuture

object CommandBuilderContextImpl : CommandBuilderContext {
    context(GunpowderModule)
    override fun command(vararg names: String, dedicatedOnly: Boolean, block: CommandBuilderContext.CommandBuilder.() -> Unit) {
        CommandRegistrationCallback.EVENT.register { dispatcher, isDedicated ->
            if (dedicatedOnly && !isDedicated) return@register

            for (name in names) {
                val node = LiteralArgumentBuilder.literal<ServerCommandSource>(name)
                val builder = CommandBuilderImpl(node, this@GunpowderModule)
                builder.block()
                dispatcher.register(node)
            }
        }
    }

    private open class CommandBuilderImpl(protected val root: ArgumentBuilder<ServerCommandSource, *>, private val module: GunpowderModule) : CommandBuilderContext.CommandBuilder {
        init {
            root.requires { module.enabled }
        }

        override fun literal(vararg literals: String, block: CommandBuilderContext.CommandBuilder.() -> Unit) {
            for (literal in literals) {
                val node = LiteralArgumentBuilder.literal<ServerCommandSource>(literal)
                val builder = CommandBuilderImpl(node, module)
                builder.block()
                root.then(node)
            }
        }

        override fun <T> argumentImpl(name: String, type: ArgumentType<T>, clazz: Class<T>, block: CommandBuilderContext.ArgumentCommandBuilder.(CommandContext<ServerCommandSource>.() -> T) -> Unit) {
            val node = RequiredArgumentBuilder.argument<ServerCommandSource, T>(name, type)
            val builder = ArgumentCommandBuilderImpl(node, module)
            builder.block {
                this.getArgument(name, clazz)
            }
            root.then(node)
        }

        override fun <T> optArgumentImpl(
            name: String,
            type: ArgumentType<T>,
            clazz: Class<T>,
            block: CommandBuilderContext.ArgumentCommandBuilder.(CommandContext<ServerCommandSource>.() -> T?) -> Unit
        ) {
            val node = RequiredArgumentBuilder.argument<ServerCommandSource, T>(name, type)
            val builder = ArgumentCommandBuilderImpl(node, module)
            builder.block {
                this.getArgument(name, clazz)
            }
            root.then(node)
            val rootBuilder = ArgumentCommandBuilderImpl(root, module, false)
            rootBuilder.block {
                null
            }
        }

        override fun requires(block: (ServerCommandSource) -> Boolean) {
            root.requires {
                module.enabled && block(it)
            }
        }

        override fun executes(block: CommandContext<ServerCommandSource>.() -> Int) {
            root.executes(block)
        }
    }

    private class ArgumentCommandBuilderImpl(private val root2: ArgumentBuilder<ServerCommandSource, *>, module: GunpowderModule, private val isArgument: Boolean = true) : CommandBuilderImpl(root2, module), CommandBuilderContext.ArgumentCommandBuilder {
        override fun suggests(block: CommandContext<ServerCommandSource>.(SuggestionsBuilder) -> CompletableFuture<Suggestions>) {
            if (isArgument) {
                (root2 as RequiredArgumentBuilder<ServerCommandSource, *>).suggests(block)
            }
        }
    }
}
