package io.github.gunpowder.exposed.column

import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtIo
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import org.jetbrains.exposed.sql.vendors.currentDialect
import java.io.*
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
