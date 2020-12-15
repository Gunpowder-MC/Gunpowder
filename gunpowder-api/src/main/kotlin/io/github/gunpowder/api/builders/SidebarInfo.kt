/*
 * MIT License
 *
 * Copyright (c) GunpowderMC
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

package io.github.gunpowder.api.builders

import io.github.gunpowder.api.GunpowderMod
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit
import java.util.function.Consumer

interface SidebarInfo {
    companion object {
        /**
         * Create a sidebar factory
         */
        @JvmStatic
        fun factory(callback: Consumer<Builder>) = factory(callback::accept)
        fun factory(callback: Builder.() -> Unit): (ServerPlayerEntity) -> SidebarInfo {
            val builder = GunpowderMod.instance.registry.getBuilder(Builder::class.java)
            callback(builder)
            return builder::build
        }
    }

    interface Builder {
        /**
         * Maximum of 16 characters, unchecked
         */
        fun title(text: String) = title(text, Formatting.RESET)
        fun title(text: String, color: Formatting)

        /**
         * Fancy version
         */
        fun displayTitle(text: Text)

        /**
         * Add a line
         */
        fun line(text: String) = line(text, Formatting.RESET)
        fun line(text: String, color: Formatting)

        @Deprecated("Used internally, do not use.")
        fun build(player: ServerPlayerEntity): SidebarInfo
    }

    /**
     * Hide the sidebar
     */
    fun remove()

    /**
     * Hide the sidebar after a specified amount of time
     */
    fun removeAfter(seconds: Long) = removeAfter(seconds, ChronoUnit.SECONDS)
    fun removeAfter(time: Long, unit: TemporalUnit)
}
