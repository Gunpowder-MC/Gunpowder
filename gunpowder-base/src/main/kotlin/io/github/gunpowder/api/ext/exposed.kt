package io.github.gunpowder.api.ext

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface ColumnHandler {
    fun blockPos(table: Table, name: String): Column<BlockPos>
    fun identifier(table: Table, name: String, collate: String? = null, eagerLoading: Boolean = false): Column<Identifier>
    fun itemStack(table: Table, name: String): Column<ItemStack>
    fun nbtCompound(table: Table, name: String): Column<NbtCompound>
}

private object ColumnHandlerProvider : KoinComponent {
    val handler by inject<ColumnHandler>()
}

fun Table.blockPos(name: String) : Column<BlockPos> {
    return ColumnHandlerProvider.handler.blockPos(this, name)
}

fun Table.identifier(name: String, collate: String? = null, eagerLoading: Boolean = false) : Column<Identifier> {
    return ColumnHandlerProvider.handler.identifier(this, name, collate, eagerLoading)
}

fun Table.itemStack(name: String) : Column<ItemStack> {
    return ColumnHandlerProvider.handler.itemStack(this, name)
}

fun Table.nbtCompound(name: String): Column<NbtCompound> {
    return ColumnHandlerProvider.handler.nbtCompound(this, name)
}
