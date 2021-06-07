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

package io.github.gunpowder.api.exposed

import io.github.gunpowder.api.components.Component
import io.github.gunpowder.api.exposed.typeimpl.BlockPosColumnType
import io.github.gunpowder.api.exposed.typeimpl.CompoundTagColumnType
import io.github.gunpowder.api.exposed.typeimpl.IdentifierColumnType
import io.github.gunpowder.api.exposed.typeimpl.ItemStackColumnType
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

fun Table.blockPos(name: String) : Column<BlockPos> {
    return registerColumn(name, BlockPosColumnType())
}

fun Table.identifier(name: String, collate: String? = null, eagerLoading: Boolean = false) : Column<Identifier> {
    return registerColumn(name, IdentifierColumnType(collate, eagerLoading))
}

fun Table.compoundTag(name: String): Column<CompoundTag> {
    return registerColumn(name, CompoundTagColumnType())
}

fun Table.itemStack(name: String) : Column<ItemStack> {
    return registerColumn(name, ItemStackColumnType())
}
