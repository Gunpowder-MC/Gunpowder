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

package io.github.gunpowder.api.exposed.typeimpl

import com.google.common.io.ByteStreams
import net.minecraft.item.ItemStack
import net.minecraft.nbt.*
import net.minecraft.util.Identifier
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import org.jetbrains.exposed.sql.vendors.currentDialect
import java.io.*
import java.sql.Blob
import java.sql.ResultSet

open class NbtCompoundColumnType : ColumnType() {
    override fun sqlType(): String = currentDialect.dataTypeProvider.blobType()

    override fun valueFromDB(value: Any): Any {
        val blob = when (value) {
            is ByteArray -> value
            else -> error("Unexpected value of type Blob: $value of ${value::class.qualifiedName}")
        }

        return NbtIo.read(
            DataInputStream(
                ByteArrayInputStream(blob)
            )
        )
    }

    override fun valueToDB(value: Any?): Any? {
        return when(value) {
            is NbtCompound -> {
                val stream = ByteArrayOutputStream()
                NbtIo.write(value, DataOutputStream(stream))
                return ByteArrayInputStream(stream.toByteArray())
            }
            else -> super.valueToDB(value)
        }
    }

    override fun readObject(rs: ResultSet, index: Int): ByteArray = rs.getBytes(index)

    override fun setParameter(stmt: PreparedStatementApi, index: Int, value: Any?) {
        when (value) {
            is InputStream -> stmt.setInputStream(index, value)
            null -> stmt.setNull(index, this)
        }
    }
}
