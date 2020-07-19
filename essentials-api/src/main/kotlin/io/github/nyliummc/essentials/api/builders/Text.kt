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

package io.github.nyliummc.essentials.api.builders

import io.github.nyliummc.essentials.api.EssentialsMod
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.util.Formatting
import net.minecraft.text.Text as MCText


interface Text {
    companion object {
        @JvmStatic
        fun builder(callback: Builder.() -> Unit): MCText {
            val builder = EssentialsMod.instance.registry.getBuilder(Builder::class.java)
            callback(builder)
            return builder.build()
        }
    }

    interface Builder {
        /**
         * Add text.
         */
        fun text(text: String)
        fun text(text: MCText)

        /**
         * Add styled text.
         */
        fun text(text: String, callback: StyleBuilder.() -> Unit)
        fun text(text: MCText, callback: StyleBuilder.() -> Unit)

        @Deprecated("Used internally, do not use.")
        fun build(): MCText
    }

    interface StyleBuilder {
        /**
         * Run a command when clicked.
         */
        fun onClickCommand(command: String)

        /**
         * Tooltip to show when hovering.
         */
        fun onHoverText(tooltip: String)

        /**
         * Color of text.
         * TODO: RGB support since 1.16
         */
        fun color(color: Formatting)


        // Various effects
        fun bold()
        fun italic()
        fun strikethrough()
        fun underlined()
        fun obfuscated()

        /**
         * Add an event handler when clicking this text.
         */
        fun onClickEvent(event: ClickEvent)

        /**
         * Add an event handler when hovering over this text.
         */
        fun onHoverEvent(event: HoverEvent)
    }
}
