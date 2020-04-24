package io.github.nyliummc.essentials.models

import org.jetbrains.exposed.sql.Table

object BalanceTable : Table() {
    val user = uuid("user")
    val balance = decimal("balance", 99, 2)
}
