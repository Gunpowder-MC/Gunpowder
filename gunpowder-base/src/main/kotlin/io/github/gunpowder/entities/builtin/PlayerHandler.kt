/*
 * MIT License
 *
 * Copyright (c) GunpowderMC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.gunpowder.entities.builtin

import io.github.gunpowder.api.exposed.PlayerTable
import io.github.gunpowder.entities.database.GunpowderServerDatabase
import net.minecraft.server.network.ServerPlayerEntity
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime

object PlayerHandler {
    private val db by lazy {
        GunpowderServerDatabase
    }

    fun registerPlayer(player: ServerPlayerEntity) {
        db.transaction {
            val row = PlayerTable.select { PlayerTable.id.eq(player.uuid) }.firstOrNull()
            if (row != null) {
                PlayerTable.update({ PlayerTable.id.eq(player.uuid) }) {
                    it[PlayerTable.lastSeen] = LocalDateTime.now()
                    it[PlayerTable.username] = player.entityName
                }
            } else {
                PlayerTable.insert {
                    it[PlayerTable.id] = player.uuid
                    it[PlayerTable.username] = player.entityName
                    it[PlayerTable.firstSeen] = LocalDateTime.now()
                    it[PlayerTable.lastSeen] = LocalDateTime.now()
                }
            }
        }
    }

    fun lastSeenPlayer(player: ServerPlayerEntity) {
        db.transaction {
            PlayerTable.update({ PlayerTable.id.eq(player.uuid) }) {
                it[PlayerTable.lastSeen] = LocalDateTime.now()
            }
        }
    }
}
