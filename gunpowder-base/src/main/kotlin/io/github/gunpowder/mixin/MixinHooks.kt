package io.github.gunpowder.mixin

import io.github.gunpowder.api.GunpowderDatabase
import io.github.gunpowder.api.GunpowderMod
import io.github.gunpowder.api.tables.PlayerTable
import net.minecraft.server.network.ServerPlayerEntity
import org.jetbrains.exposed.sql.insertIgnore
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object MixinHooks : KoinComponent {
    private val db by inject<GunpowderDatabase>()

    @JvmStatic
    fun createPlayerEntry(player: ServerPlayerEntity) {
        db.transaction {
            PlayerTable.insertIgnore {
                it[PlayerTable.uuid] = player.uuid
                it[PlayerTable.username] = player.name.asString()
            }
        }
    }
}
