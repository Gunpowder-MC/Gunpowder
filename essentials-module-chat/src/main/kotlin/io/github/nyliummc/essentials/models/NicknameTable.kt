package io.github.nyliummc.essentials.models

import org.jetbrains.exposed.sql.Table

object NicknameTable : Table() {
    val user = uuid("user")
    val nickname = varchar("nickname", 255)
}