package io.github.gunpowder.api.types

import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

interface ChestGUI {
    data class Button(val x: Int, val y: Int, val icon: ItemStack, val onClick: (SlotActionType) -> Unit)

    val displayName: Text
    val buttons: List<Button>
    val empty: ItemStack
        get() = ItemStack.EMPTY
    val tickInterval: TimeUnit
        get() = TimeUnit.NEVER
    val rows: Int
        get() = 6

    fun open(player: ServerPlayerEntity)
    /**
     * return true if any buttons were updated
     */
    fun tick(): Boolean
    fun close()
}
