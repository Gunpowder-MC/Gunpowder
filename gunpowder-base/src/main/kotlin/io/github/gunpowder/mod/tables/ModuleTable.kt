package io.github.gunpowder.mod.tables

import org.jetbrains.exposed.sql.Table

object ModuleTable : Table() {
    val id = varchar("id", 64)
    val enabled = bool("enabled")

    override val primaryKey = PrimaryKey(id)
}
