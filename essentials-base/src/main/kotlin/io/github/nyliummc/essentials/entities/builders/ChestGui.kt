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

import io.github.nyliummc.essentials.api.util.ContainerUtil
import io.github.nyliummc.essentials.entities.mc.ChestGuiContainer
import net.minecraft.container.Container
import net.minecraft.container.SlotActionType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import io.github.nyliummc.essentials.api.builders.ChestGui as APIChestGui

object ChestGui : APIChestGui {

    class Builder : APIChestGui.Builder {
        internal data class ChestGuiButton(val icon: ItemStack, val callback: (SlotActionType) -> Unit)

        private val buttons = mutableMapOf<Int, ChestGuiButton>()
        private var icon: ItemStack = ItemStack.EMPTY
        private var player: PlayerEntity? = null
        private var syncId: Int = 0

        override fun player(player: ServerPlayerEntity) {
            this.syncId = ContainerUtil.getSyncId(player)
            this.player = player
        }

        override fun button(x: Int, y: Int, icon: ItemStack, clickCallback: (SlotActionType) -> Unit) {
            if (x < 0 || x > 8) {
                throw AssertionError("X not between 0 and 8")
            }

            if (y < 0 || y > 5) {
                throw AssertionError("Y not between 0 and 5")
            }

            buttons[x + y * 9] = ChestGuiButton(icon, clickCallback)
        }

        override fun emptyIcon(icon: ItemStack) {
            this.icon = icon
        }

        override fun build(): Container {
            return ChestGuiContainer(syncId, player!!.inventory).apply {
                setBackground(icon)
                setButtons(buttons)
                createInventory()
            }
        }

    }
}
