package io.github.gunpowder.api.builders

import net.minecraft.text.BaseText
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.Text
import net.minecraft.util.Formatting

interface TextBuilderContext {
    fun build(block: TextBuilder.() -> Unit): Text

    interface TextBuilder {
        fun text(text: String, block: StyleBuilderContext.() -> Unit = {})
        fun text(text: BaseText, block: StyleBuilderContext.() -> Unit = {})
    }

    interface StyleBuilderContext {
        fun formatting(formatting: Formatting)
        fun formatting(formatString: String)
        fun onClickCommand(command: String)
        fun clickEvent(event: ClickEvent)
        fun hoverEvent(event: HoverEvent)
    }
}
