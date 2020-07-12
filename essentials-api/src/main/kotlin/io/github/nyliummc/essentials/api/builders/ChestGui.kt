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
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.server.network.ServerPlayerEntity

interface ChestGui {
    companion object {
        /**
         * Creates a ChestGui
         *
         * @param callback Block in scope of a {@link io.github.nyliummc.essentials.api.builders.ChestGui.Builder}
         * @return A Screenhandler that can be sent to the client
         */
        @JvmStatic
        fun builder(callback: Builder.() -> Unit): ScreenHandler {
            val builder = EssentialsMod.instance.registry.getBuilder(Builder::class.java)
            callback(builder)
            return builder.build()
        }

        /**
         * Creates a ChestGui Factory
         *
         * @param callback Block in scope of a {@link io.github.nyliummc.essentials.api.builders.ChestGui.Builder}
         * @return a (ServerPlayerEntity) -> ScreenHandler lambda
         */
        @JvmStatic
        fun factory(callback: Builder.() -> Unit): (ServerPlayerEntity) -> ScreenHandler {
            val builder = EssentialsMod.instance.registry.getBuilder(Builder::class.java)
            callback(builder)
            return {
                builder.player(it)
                builder.build()
            }
        }
    }

    interface Builder {
        /**
         * Set the player to sync the GUI with
         */
        fun player(player: ServerPlayerEntity)

        /**
         * Add an item as button
         *
         * @param x the column in the chest
         * @param y the row in the chest
         * @param icon the ItemStack to display
         * @param clickCallback the callback to execute when clicked
         */
        fun button(x: Int, y: Int, icon: ItemStack, clickCallback: (SlotActionType) -> Unit)

        /**
         * The default itemstack for unspecified buttons. ItemStack.EMPTY by default.
         */
        fun emptyIcon(icon: ItemStack)

        @Deprecated("Used internally, do not use.")
        fun build(): ScreenHandler
    }
}
