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

package io.github.gunpowder.entities.mc

import io.github.gunpowder.entities.GunpowderEvents
import io.github.gunpowder.api.builders.ContainerGui as APIChestGui
import io.github.gunpowder.entities.builders.ContainerGui
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.screen.ScreenHandlerListener
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.server.network.ServerPlayerEntity


class ContainerGuiScreen(type: ScreenHandlerType<GenericContainerScreenHandler>,
                         syncId: Int, playerInventory: PlayerInventory) : GenericContainerScreenHandler(type, syncId, playerInventory, SimpleInventory(54), 6), APIChestGui.Container {
    private var buttons: MutableMap<Int, ContainerGui.Builder.ChestGuiButton> = mutableMapOf()
    private var background = ItemStack.EMPTY
    private var interval = 0
    private var counter = 0
    private var callback = { _: ContainerGuiScreen -> }

    internal fun setButtons(buttons: MutableMap<Int, ContainerGui.Builder.ChestGuiButton>) {
        this.buttons = buttons
    }

    internal fun setBackground(item: ItemStack) {
        background = item
    }

    internal fun setInterval(seconds: Int, callback: (APIChestGui.Container) -> Unit) {
        interval = seconds
        this.callback = callback
    }

    internal fun createInventory() {
        for (i in 0 until 54) {
            inventory.setStack(i, buttons[i]?.icon ?: background)
        }
    }

    override fun canUse(player: PlayerEntity?): Boolean {
        return true
    }

    override fun addListener(listener: ScreenHandlerListener?) {
        GunpowderEvents.guis.add(this)
        super.addListener(listener)
    }

    override fun removeListener(listener: ScreenHandlerListener?) {
        GunpowderEvents.guis.remove(this)
        super.removeListener(listener)
    }

    fun update() {
        if (interval <= 0) return
        if (counter++ >= interval) {
            sendContentUpdates()
            counter = 0
        }
    }

    override fun onSlotClick(slotId: Int, clickData: Int, actionType: SlotActionType, playerEntity: PlayerEntity): ItemStack {
        if (buttons.containsKey(slotId)) {
            val button = buttons[slotId]!!
            button.callback.invoke(actionType)
        }

        // Avoid desyncs
        inventory.markDirty()
        (playerEntity as ServerPlayerEntity).server.playerManager.sendToAll(InventoryS2CPacket(syncId, playerEntity.currentScreenHandler.stacks))
        sendContentUpdates()

        return ItemStack.EMPTY
    }

    override fun clearButtons() {
        buttons.clear()
    }

    override fun button(x: Int, y: Int, icon: ItemStack, clickCallback: (SlotActionType) -> Unit) {
        if (x < 0 || x > 8) {
            throw AssertionError("X not between 0 and 8")
        }

        if (y < 0 || y > 5) {
            throw AssertionError("Y not between 0 and 5")
        }

        buttons[x + y * 9] = ContainerGui.Builder.ChestGuiButton(icon, clickCallback)
    }

    override fun emptyIcon(icon: ItemStack) {
        setBackground(icon)
    }
}
