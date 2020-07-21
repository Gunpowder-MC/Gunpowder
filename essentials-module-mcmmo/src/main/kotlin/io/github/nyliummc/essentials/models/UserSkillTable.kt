package io.github.nyliummc.essentials.models

import io.github.nyliummc.essentials.api.models.PlayerTable
import org.jetbrains.exposed.sql.Table

object UserSkillTable : Table() {
    val user = reference("user", PlayerTable.id)
    val skill = text("skill")
    val exp = integer("xp")
    val level = integer("level")

    override val primaryKey = PrimaryKey(user, skill)
}
