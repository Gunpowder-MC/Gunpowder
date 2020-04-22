package io.github.nyliummc.essentials.models

import org.jetbrains.exposed.sql.Table

object HomeTable : Table() {
    val owner = uuid("owner")
    val name = text("homeName")
    val x = integer("x")
    val y = integer("y")
    val z = integer("z")
    val dimension = integer("dimension")
}
