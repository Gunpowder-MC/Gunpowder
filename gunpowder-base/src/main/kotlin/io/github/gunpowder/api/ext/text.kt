package io.github.gunpowder.api.ext

import com.mojang.brigadier.context.CommandContext
import io.github.gunpowder.api.builders.TextBuilderContext
import net.minecraft.server.command.ServerCommandSource
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private object TextBuilderContextProvider : KoinComponent {
    val handler by inject<TextBuilderContext>()
}

fun CommandContext<ServerCommandSource>.feedback(sendToOps: Boolean = false, block: TextBuilderContext.TextBuilder.() -> Unit) {
    source.sendFeedback(
        TextBuilderContextProvider.handler.build(block), sendToOps
    )
}

fun CommandContext<ServerCommandSource>.error(block: TextBuilderContext.TextBuilder.() -> Unit) {
    source.sendError(
        TextBuilderContextProvider.handler.build(block)
    )
}

fun text(block: TextBuilderContext.TextBuilder.() -> Unit) = TextBuilderContextProvider.handler.build(block)
