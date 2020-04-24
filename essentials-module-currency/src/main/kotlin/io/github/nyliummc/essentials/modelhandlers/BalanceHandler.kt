package io.github.nyliummc.essentials.modelhandlers

import com.mojang.authlib.GameProfile
import io.github.nyliummc.essentials.api.modules.currency.dataholders.StoredBalance
import io.github.nyliummc.essentials.models.BalanceTable
import net.minecraft.server.MinecraftServer
import io.github.nyliummc.essentials.api.modules.currency.modelhandlers.BalanceHandler as APIBalanceHandler
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.*

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
            val users = BalanceTable.selectAll().orderBy(BalanceTable.balance, false).limit(10)
            users.map {
                StoredBalance(it[BalanceTable.user], it[BalanceTable.balance])
            }.toTypedArray()
        }
    }

    override fun modifyUser(user: UUID, callable: (StoredBalance) -> StoredBalance) {
        updateUser(callable(getUser(user)))
    }
}