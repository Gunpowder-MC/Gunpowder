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

package io.github.nyliummc.essentials.modelhandlers

import io.github.nyliummc.essentials.api.modules.chat.dataholders.StoredNickname
import io.github.nyliummc.essentials.api.modules.chat.modelhandlers.NicknameHandler as APINicknameHandler
import io.github.nyliummc.essentials.models.NicknameTable
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.*

object NicknameHandler : APINicknameHandler {
    private val cache: MutableMap<UUID, StoredNickname> = mutableMapOf()

    override fun getUser(user: UUID): StoredNickname {
        return cache[user] ?: transaction {
            val userObj = NicknameTable.select { NicknameTable.user.eq(user) }.first()
            val StoredNickname = StoredNickname(
                    user,
                    userObj[NicknameTable.nickname])
            cache[user] = StoredNickname
            StoredNickname
        }
    }

    override fun updateUser(user: StoredNickname) {
        cache[user.uuid] = user
        transaction {
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