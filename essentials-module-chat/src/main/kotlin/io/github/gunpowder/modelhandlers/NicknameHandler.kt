/*
 * MIT License
 *
 * Copyright (c) NyliumMC
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

package io.github.gunpowder.modelhandlers

import io.github.gunpowder.api.GunpowderMod
import io.github.gunpowder.api.module.chat.dataholders.StoredNickname
import io.github.gunpowder.models.NicknameTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.util.*
import io.github.gunpowder.api.module.chat.modelhandlers.NicknameHandler as APINicknameHandler

object NicknameHandler : APINicknameHandler {
    private val db by lazy {
        GunpowderMod.instance.database
    }
    private val cache: MutableMap<UUID, StoredNickname> = mutableMapOf()

    init {
        loadAllNicknames()
    }

    private fun loadAllNicknames() {
        val items = db.transaction {
            NicknameTable.selectAll().map {
                it[NicknameTable.user] to StoredNickname(it[NicknameTable.user], it[NicknameTable.nickname])
            }.toMap()
        }.get()
        cache.putAll(items)
    }

    override fun getUser(user: UUID): StoredNickname {
        return cache[user] ?: db.transaction {
            NicknameTable.insert {
                it[NicknameTable.user] = user
                it[NicknameTable.nickname] = ""
            }
            val stored = StoredNickname(
                    user,
                    "")
            cache[user] = stored
            stored
        }.get()
    }

    override fun updateUser(user: StoredNickname) {
        cache[user.uuid] = user
        db.transaction {
            NicknameTable.update({
                NicknameTable.user.eq(user.uuid)
            }) {
                it[nickname] = user.nickname
            }
        }
    }

    override fun modifyUser(user: UUID, callable: (StoredNickname) -> StoredNickname) {
        updateUser(callable(getUser(user)))
    }
}
