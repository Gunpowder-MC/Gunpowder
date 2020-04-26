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

import io.github.nyliummc.essentials.api.modules.currency.dataholders.StoredBalance
import io.github.nyliummc.essentials.models.BalanceTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.*
import io.github.nyliummc.essentials.api.modules.currency.modelhandlers.BalanceHandler as APIBalanceHandler

object BalanceHandler : APIBalanceHandler {
    private val cache: MutableMap<UUID, StoredBalance> = mutableMapOf()

    override fun getUser(user: UUID): StoredBalance {
        return cache[user] ?: transaction {
            val userObj = BalanceTable.select { BalanceTable.user.eq(user) }.first()
            val balance = StoredBalance(
                    user,
                    userObj[BalanceTable.balance])
            cache[user] = balance
            balance
        }
    }

    override fun updateUser(user: StoredBalance) {
        cache[user.uuid] = user
        transaction {
            BalanceTable.update({
                BalanceTable.user.eq(user.uuid)
            }) {
                it[balance] = user.balance
            }
        }
    }

    override fun getBalanceTop(): Array<StoredBalance> {
        return transaction {
            val users = BalanceTable.selectAll().orderBy(BalanceTable.balance, SortOrder.DESC).limit(10)
            users.map {
                StoredBalance(it[BalanceTable.user], it[BalanceTable.balance])
            }.toTypedArray()
        }
    }

    override fun modifyUser(user: UUID, callable: (StoredBalance) -> StoredBalance) {
        updateUser(callable(getUser(user)))
    }
}
