package io.github.nyliummc.essentials.api.modules.market.dataholders

import net.minecraft.item.ItemStack
import org.joda.time.DateTime
import java.math.BigDecimal
import java.util.*

class StoredMarketEntry(val uuid: UUID, val item: ItemStack, val price: BigDecimal, val expire: DateTime) {
}
