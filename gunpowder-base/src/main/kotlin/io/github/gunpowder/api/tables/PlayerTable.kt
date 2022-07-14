package io.github.gunpowder.api.tables

import org.jetbrains.exposed.sql.Table

object PlayerTable : Table() {
    val uuid = uuid("uuid").uniqueIndex()
    val username = varchar("username", 32)

    override val primaryKey = PrimaryKey(uuid)
}
