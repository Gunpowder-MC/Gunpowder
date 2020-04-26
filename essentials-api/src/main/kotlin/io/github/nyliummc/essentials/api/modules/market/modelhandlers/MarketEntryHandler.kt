package io.github.nyliummc.essentials.api.modules.market.modelhandlers

import io.github.nyliummc.essentials.api.modules.market.dataholders.StoredMarketEntry

interface MarketEntryHandler {
    fun createEntry(e: StoredMarketEntry)
    fun getEntries(): List<StoredMarketEntry>
    fun deleteEntry(e: StoredMarketEntry)
}
