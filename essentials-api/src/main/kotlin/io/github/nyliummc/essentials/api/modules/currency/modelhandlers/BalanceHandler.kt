package io.github.nyliummc.essentials.api.modules.currency.modelhandlers

import io.github.nyliummc.essentials.api.modules.currency.dataholders.StoredBalance
import java.util.*

interface BalanceHandler {
    fun getUser(user: UUID): StoredBalance
    fun updateUser(user: StoredBalance)
    fun modifyUser(user: UUID, callable: (StoredBalance) -> StoredBalance)
    fun getBalanceTop(): Array<StoredBalance>
}