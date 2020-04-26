package io.github.nyliummc.essentials.modelhandlers

import com.google.gson.annotations.Expose
import io.github.nyliummc.essentials.api.EssentialsMod
import io.github.nyliummc.essentials.api.modules.market.dataholders.StoredMarketEntry
import io.github.nyliummc.essentials.api.modules.market.modelhandlers.MarketEntryHandler as APIMarketEntryHandler
import io.github.nyliummc.essentials.models.MarketEntryTable
import net.fabricmc.fabric.api.util.NbtType
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.StringTag
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.ByteArrayOutputStream
import java.time.Duration
import java.time.LocalDateTime
import kotlin.time.toKotlinDuration

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
    private fun loadItemStack(blob: ExposedBlob) : ItemStack {
        val tag = NbtIo.readCompressed(blob.bytes.inputStream())
        return ItemStack.fromTag(tag)
    }

    private fun saveItemStack(stack: ItemStack): ExposedBlob {
        val tag = CompoundTag()
        val stream = ByteArrayOutputStream()
        stack.toTag(tag)
        NbtIo.writeCompressed(tag, stream)
        return ExposedBlob(stream.toByteArray())
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
        return cache.toList().sortedBy { it.expire }.also {
            it.forEach { entry ->
                val seller = EssentialsMod.instance!!.server.userCache.getByUuid(entry.uuid)!!.name
                val timeLeft = Duration.between(LocalDateTime.now(), entry.expire)
                val timeString = "${timeLeft.toDays()} days, ${timeLeft.toHours() % 24} hours, " +
                                 "${timeLeft.toMinutes() % 60} minutes and ${timeLeft.seconds % 60} seconds"

                // Add Lore
                val tag = entry.item.tag ?: CompoundTag()
                val display = tag.get("display") as CompoundTag? ?: CompoundTag()
                val lore = tag.get("Lore") as ListTag? ?: ListTag()
                val newLore = ListTag()

                newLore.addAll(
                        // Add our stuff
                        listOf(
                            StringTag.of("[{\"text\":\"\"}]"),  // Blank line
                            // Seller
                            StringTag.of("[{\"text\":\"Seller: \",\"color\":\"white\",\"italic\":false},{\"text\":\"$seller\",\"color\":\"yellow\",\"italic\":false}]"),
                            // Price
                            StringTag.of("[{\"text\":\"Price: \",\"color\":\"white\",\"italic\":false},{\"text\":\"${entry.price.toDouble()}\",\"color\":\"yellow\",\"italic\":false}]"),
                            // Expire time
                            StringTag.of("[{\"text\":\"Expires in: \",\"color\":\"white\",\"italic\":false},{\"text\":\"$timeString\",\"color\":\"yellow\",\"italic\":false}]")
                        )
                )

                // Add original
                for (i in 0 until lore.lastIndex) {
                    newLore.add(lore[i])
                }

                display.put("Lore", newLore)
                tag.put("display", display)
                entry.item.tag = tag
            }
        }
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
