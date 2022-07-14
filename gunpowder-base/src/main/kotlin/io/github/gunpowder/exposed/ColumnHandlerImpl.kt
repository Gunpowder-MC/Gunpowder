package io.github.gunpowder.exposed

import io.github.gunpowder.api.ext.ColumnHandler
import io.github.gunpowder.exposed.column.BlockPosColumnType
import io.github.gunpowder.exposed.column.IdentifierColumnType
import io.github.gunpowder.exposed.column.ItemStackColumnType
import io.github.gunpowder.exposed.column.NbtCompoundColumnType
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object ColumnHandlerImpl : ColumnHandler {
    override fun blockPos(table: Table, name: String): Column<BlockPos> {
        return table.registerColumn(name, BlockPosColumnType())
    }

    override fun identifier(table: Table, name: String, collate: String?, eagerLoading: Boolean): Column<Identifier> {
        return table.registerColumn(name, IdentifierColumnType(collate, eagerLoading))
    }

    override fun itemStack(table: Table, name: String): Column<ItemStack> {
        return table.registerColumn(name, ItemStackColumnType())
    }

    override fun nbtCompound(table: Table, name: String): Column<NbtCompound> {
        return table.registerColumn(name, NbtCompoundColumnType())
    }
}
