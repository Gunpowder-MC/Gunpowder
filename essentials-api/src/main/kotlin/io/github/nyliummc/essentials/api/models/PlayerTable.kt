package io.github.nyliummc.essentials.api.models

import org.jetbrains.exposed.sql.Table

object PlayerTable : Table() {
    val id = uuid("uuid").uniqueIndex()

    override val primaryKey = PrimaryKey(id)
}
