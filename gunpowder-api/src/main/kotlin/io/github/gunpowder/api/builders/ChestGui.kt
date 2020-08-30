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
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.server.network.ServerPlayerEntity
import java.util.function.Consumer

interface ChestGui {
    companion object {
        /**
         * Creates a ChestGui
         *
         * @param callback Block in scope of a {@link io.github.gunpowder.gunpowder.api.builders.ChestGui.Builder}
         * @return A ScreenHandler that can be sent to the client
         */
        @JvmStatic
        fun builder(callback: Consumer<Builder>) = builder(callback::accept)
        fun builder(callback: Builder.() -> Unit): ScreenHandler {
            val builder = GunpowderMod.instance.registry.getBuilder(Builder::class.java)
            callback(builder)
            return builder.build()
        }

        /**
         * Creates a ChestGui Factory
         *
         * @param callback Block in scope of a {@link io.github.gunpowder.gunpowder.api.builders.ChestGui.Builder}
         * @return a (ServerPlayerEntity) -> ScreenHandler lambda
         */
        @JvmStatic
        fun factory(callback: Consumer<Builder>) = factory(callback::accept)
        fun factory(callback: Builder.() -> Unit): (ServerPlayerEntity) -> ScreenHandler {
            val builder = GunpowderMod.instance.registry.getBuilder(Builder::class.java)
            callback(builder)
            return {
                builder.player(it)
                builder.build()
            }
        }
    }

    interface Builder {
        /**
         * Set the player to sync the GUI with.
         */
        fun player(player: ServerPlayerEntity)

        /**
         * Add an item as button.
         *
         * @param x the column in the chest
         * @param y the row in the chest
         * @param icon the ItemStack to display
         * @param clickCallback the callback to execute when clicked
         */
        fun button(x: Int, y: Int, icon: ItemStack, clickCallback: Consumer<SlotActionType>) = button(x, y, icon, clickCallback::accept)
        fun button(x: Int, y: Int, icon: ItemStack, clickCallback: (SlotActionType) -> Unit)

        /**
         * The default itemstack for unspecified buttons. ItemStack.EMPTY by default.
         */
        fun emptyIcon(icon: ItemStack)

        /**
         * Time in seconds between sending contents. 0 to disable
         */
        fun refreshInterval(seconds: Int, callback: Consumer<Container>) = refreshInterval(seconds, callback::accept)
        fun refreshInterval(seconds: Int, callback: (Container) -> Unit)

        /**
         * Sets the amount of rows used in the gui. 6 by default.
         * Must be at least 1 and at most 6.
         */
        fun size(rows: Int)

        @Deprecated("Used internally, do not use.")
        fun build(): ScreenHandler
    }

    interface Container {
        fun clearButtons()
        fun button(x: Int, y: Int, icon: ItemStack, clickCallback: Consumer<SlotActionType>) = button(x, y, icon, clickCallback::accept)
        fun button(x: Int, y: Int, icon: ItemStack, clickCallback: (SlotActionType) -> Unit)
        fun emptyIcon(icon: ItemStack)
    }
}
