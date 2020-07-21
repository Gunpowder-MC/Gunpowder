package io.github.nyliummc.essentials.entities

import io.github.nyliummc.essentials.api.models.PlayerTable
import net.minecraft.server.network.ServerPlayerEntity
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

object KotlinEventHandler {
    @JvmStatic
    fun playerConnect(p: ServerPlayerEntity) {
        val pl = PlayerTable.select { PlayerTable.id.eq(p.uuid) }.firstOrNull()
        if (pl == null) {
            // Add user to table
            PlayerTable.insert {
                it[PlayerTable.id] = p.uuid
            }
        }
    }
}
