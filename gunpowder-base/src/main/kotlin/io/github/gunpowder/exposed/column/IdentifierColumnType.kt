package io.github.gunpowder.exposed.column

import net.minecraft.util.Identifier
import org.jetbrains.exposed.sql.TextColumnType

class IdentifierColumnType(collate: String? = null, eagerLoading: Boolean = false) : TextColumnType(collate, eagerLoading) {
    override fun valueFromDB(value: Any): Any {
        return when (val res = super.valueFromDB(value)) {
            is String -> Identifier(res)
            else -> res
        }
    }

    override fun valueToDB(value: Any?): Any? = when (value) {
        is Identifier -> super.valueToDB(value.toString())
        else -> super.valueToDB(value)
    }
}
