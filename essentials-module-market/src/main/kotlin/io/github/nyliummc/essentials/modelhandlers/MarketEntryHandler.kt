package io.github.nyliummc.essentials.modelhandlers

import io.github.nyliummc.essentials.api.modules.market.dataholders.StoredMarketEntry
import io.github.nyliummc.essentials.api.modules.market.modelhandlers.MarketEntryHandler as APIMarketEntryHandler
import io.github.nyliummc.essentials.models.MarketEntryTable
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtIo
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.ByteArrayOutputStream
import java.sql.Blob
import javax.sql.rowset.serial.SerialBlob

object MarketEntryHandler : APIMarketEntryHandler {
    private val cache = mutableListOf<StoredMarketEntry>()

    init {
        loadEntries()
    }

    private fun loadEntries() {
        transaction {
            cache.addAll(MarketEntryTable.selectAll().map {
                StoredMarketEntry(
                        it[MarketEntryTable.user],
                        loadItemStack(it[MarketEntryTable.item]),
                        it[MarketEntryTable.price],
                        it[MarketEntryTable.expiresAt]
                )
            }.toList())
        }
    }

    // TODO: Move these two to util funcs
    private fun loadItemStack(blob: Blob) : ItemStack {
        val tag = NbtIo.readCompressed(blob.binaryStream)
        return ItemStack.fromTag(tag)
    }

    private fun saveItemStack(stack: ItemStack) : Blob {
        val tag = CompoundTag()
        val ostream = ByteArrayOutputStream()
        stack.toTag(tag)
        NbtIo.writeCompressed(tag, ostream)
        return SerialBlob(ostream.toByteArray())
    }

    override fun createEntry(e: StoredMarketEntry) {
        cache.add(e)
        transaction {
            MarketEntryTable.insert {
                it[user] = e.uuid
                it[item] = saveItemStack(e.item)
                it[price] = e.price
                it[expiresAt] = e.expire
            }
        }
    }

    override fun getEntries(): List<StoredMarketEntry> {
        return cache.toList().sortedBy { it.expire }
    }

    override fun deleteEntry(e: StoredMarketEntry) {
        cache.remove(e)
        transaction {
            MarketEntryTable.deleteWhere {
                MarketEntryTable.expiresAt.eq(e.expire)
            }
        }
    }
}
