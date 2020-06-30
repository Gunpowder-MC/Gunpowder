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

package io.github.nyliummc.essentials.entities.builders

import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.LiteralText
import net.minecraft.util.Formatting
import io.github.nyliummc.essentials.api.builders.Text as APIText
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
            text.style.withClickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
        }

        override fun onClickEvent(event: ClickEvent) {
            text.style.withClickEvent(event)
        }

        override fun onHoverText(tooltip: String) {
            text.style.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, LiteralText(tooltip))
        }

        override fun onHoverEvent(event: HoverEvent) {
            text.style.hoverEvent = event
        }

        override fun color(color: Formatting) {
            text.style.withColor(color)
        }

        override fun bold() {
            text.style.withBold(true)
        }

        override fun italic() {
            text.style.withItalic(true)
        }

        override fun strikethrough() {
            text.style.withFormatting(Formatting.STRIKETHROUGH)
        }

        override fun underlined() {
            text.style.withFormatting(Formatting.UNDERLINE)
        }

        override fun obfuscated() {
            text.style.withFormatting(Formatting.OBFUSCATED)
        }

    }
}
