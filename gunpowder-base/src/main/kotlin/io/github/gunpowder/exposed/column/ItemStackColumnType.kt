package io.github.gunpowder.exposed.column

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound

class ItemStackColumnType : NbtCompoundColumnType() {
    override fun valueFromDB(value: Any): Any {
        val nbt = super.valueFromDB(value) as NbtCompound
        return ItemStack.fromNbt(nbt)
    }

    override fun valueToDB(value: Any?): Any? {
        return when(value) {
            is ItemStack -> {
                val ct = NbtCompound()
                value.writeNbt(ct)
                return super.valueToDB(ct)
            }
            else -> super.valueToDB(value)
        }
    }
}
