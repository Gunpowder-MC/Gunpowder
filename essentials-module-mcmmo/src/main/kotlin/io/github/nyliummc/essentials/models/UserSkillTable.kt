package io.github.nyliummc.essentials.models

import org.jetbrains.exposed.sql.Table

object UserSkillTable : Table() {
    val user = uuid("user")  // TODO: Foreign table ref
    val skill = text("skill")  // TODO: Foreign table ref
    val exp = integer("xp")
    val level = integer("level")
}
