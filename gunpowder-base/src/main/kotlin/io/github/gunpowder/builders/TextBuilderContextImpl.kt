package io.github.gunpowder.builders

import io.github.gunpowder.api.builders.TextBuilderContext
import net.minecraft.text.BaseText
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object TextBuilderContextImpl : TextBuilderContext {
    override fun build(block: TextBuilderContext.TextBuilder.() -> Unit): Text {
        return TextBuilderImpl().also(block).build()
    }

    class TextBuilderImpl : TextBuilderContext.TextBuilder {
        private val final = LiteralText("")

        override fun text(text: String, block: TextBuilderContext.StyleBuilderContext.() -> Unit) {
            text(LiteralText(text), block)
        }

        override fun text(text: BaseText, block: TextBuilderContext.StyleBuilderContext.() -> Unit) {
            final.append(StyleBuilderContextImpl(text).also(block).build())
        }

        fun build(): Text {
            return final
        }
    }

    class StyleBuilderContextImpl(private val node: BaseText) : TextBuilderContext.StyleBuilderContext {
        override fun formatting(formatting: Formatting) {
            node.styled {
                it.withFormatting(formatting)
            }
        }

        override fun formatting(formatString: String) {
            formatting(Formatting.values().first { it.toString() == formatString })
        }

        override fun onClickCommand(command: String) {
            clickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
        }

        override fun clickEvent(event: ClickEvent) {
            node.styled {
                it.withClickEvent(event)
            }
        }

        override fun hoverEvent(event: HoverEvent) {
            node.styled {
                it.withHoverEvent(event)
            }
        }

        fun build(): Text {
            return node
        }
    }
}
