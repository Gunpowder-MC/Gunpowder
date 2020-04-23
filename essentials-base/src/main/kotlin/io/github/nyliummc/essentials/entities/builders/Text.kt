package io.github.nyliummc.essentials.entities.builders

import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import io.github.nyliummc.essentials.api.builders.Text as APIText
import net.minecraft.text.LiteralText
import net.minecraft.util.Formatting
import net.minecraft.text.Text as MCText

object Text : APIText {

    class Builder : APIText.Builder {
        private val allText = mutableListOf<MCText>()

        override fun text(text: String) {
            allText.add(LiteralText(text))
        }

        override fun text(text: MCText) {
            allText.add(text)
        }

        override fun text(text: String, callback: APIText.StyleBuilder.() -> Unit) {
            val t = LiteralText(text)
            val b = StyleBuilder(t)
            callback(b)
            allText.add(t)
        }

        override fun text(text: MCText, callback: APIText.StyleBuilder.() -> Unit) {
            val b = StyleBuilder(text)
            callback(b)
            allText.add(text)
        }

        override fun build(): MCText {
            val final = LiteralText("")
            allText.forEach {
                final.append(it)
            }
            return final
        }
    }

    class StyleBuilder(var text: MCText) : APIText.StyleBuilder {
        override fun onClickCommand(command: String) {
            text.style.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, command)
        }

        override fun onClickEvent(event: ClickEvent) {
            text.style.clickEvent = event
        }

        override fun onHoverText(tooltip: String) {
            text.style.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, LiteralText(tooltip))
        }

        override fun onHoverEvent(event: HoverEvent) {
            text.style.hoverEvent = event
        }

        override fun color(color: Formatting) {
            text.style.color = color
        }

        override fun bold() {
            text.style.isBold = true
        }

        override fun italic() {
            text.style.isItalic = true
        }

        override fun strikethrough() {
            text.style.isStrikethrough = true
        }

        override fun underlined() {
            text.style.setUnderline(true)
        }

        override fun obfuscated() {
            text.style.isObfuscated = true
        }

    }
}