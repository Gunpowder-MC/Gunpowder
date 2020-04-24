package io.github.nyliummc.essentials.api.modules.currency.dataholders

import java.math.BigDecimal
import java.util.*

data class StoredBalance(
        val uuid: UUID,
        var balance: BigDecimal
)