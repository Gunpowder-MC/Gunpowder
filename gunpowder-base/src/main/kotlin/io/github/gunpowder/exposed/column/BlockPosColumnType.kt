package io.github.gunpowder.exposed.column

import net.minecraft.util.math.BlockPos
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.vendors.currentDialect

class BlockPosColumnType : ColumnType() {
    override fun sqlType(): String = currentDialect.dataTypeProvider.longType()
    override fun valueFromDB(value: Any): BlockPos = when (value) {
        is Long -> BlockPos.fromLong(value)
        is Number -> {
            BlockPos.fromLong(value.toLong())
        }
        else -> error("Unexpected value of type BlockPos: $value of ${value::class.qualifiedName}")
    }

    override fun valueToDB(value: Any?): Long? = when (value) {
        is BlockPos -> value.asLong()
        null -> null
        else -> error("Unexpected value of type BlockPos: $value of ${value::class.qualifiedName}")
    }
}
