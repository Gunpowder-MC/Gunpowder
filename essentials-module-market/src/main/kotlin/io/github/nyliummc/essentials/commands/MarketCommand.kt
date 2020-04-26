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

package io.github.nyliummc.essentials.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import io.github.nyliummc.essentials.api.EssentialsMod
import io.github.nyliummc.essentials.api.builders.ChestGui
import io.github.nyliummc.essentials.api.builders.Command
import io.github.nyliummc.essentials.api.modules.currency.modelhandlers.BalanceHandler
import io.github.nyliummc.essentials.api.modules.market.dataholders.StoredMarketEntry
import io.github.nyliummc.essentials.api.modules.market.modelhandlers.MarketEntryHandler
import net.fabricmc.fabric.api.util.NbtType
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtHelper
import net.minecraft.network.packet.s2c.play.OpenContainerS2CPacket
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.LiteralText
import net.minecraft.util.ItemScatterer
import java.lang.Exception
import java.time.LocalDateTime

object MarketCommand {
    val marketHandler by lazy {
        EssentialsMod.instance!!.registry.getModelHandler(MarketEntryHandler::class.java)
    }
    val balanceHandler by lazy {
        EssentialsMod.instance!!.registry.getModelHandler(BalanceHandler::class.java)
    }
    val maxEntriesPerUser = 5

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        Command.builder(dispatcher) {
            command("market", "m") {
                executes(::viewMarket)

                literal("add", "a") {
                    argument("price", DoubleArgumentType.doubleArg(0.0)) {
                        executes { addMarket(it, 0) }

                        argument("amount", IntegerArgumentType.integer(0, 64)) {
                            executes(::addMarketAmount)
                        }
                    }
                }
            }
        }
    }

    private fun addMarketAmount(context: CommandContext<ServerCommandSource>): Int {
        return addMarket(context, IntegerArgumentType.getInteger(context, "amount"))
    }

    private fun addMarket(context: CommandContext<ServerCommandSource>, amount: Int): Int {
        // TODO: Smart item selection
        //       Correct amount
        if (marketHandler.getEntries().count { it.uuid == context.source.player.uuid } >= maxEntriesPerUser) {
            context.source.sendError(LiteralText("You already have the maximum of $maxEntriesPerUser entries"))
            return -1
        }

        val item = context.source.player.mainHandStack.copy()

        val entry = StoredMarketEntry(
                context.source.player.uuid,
                item,
                DoubleArgumentType.getDouble(context, "price").toBigDecimal(),
                LocalDateTime.now().plusDays(7)
        )

        // Remove from user invstack
        marketHandler.createEntry(entry)

        return 1
    }

    private fun viewMarket(context: CommandContext<ServerCommandSource>): Int {
        val entries = marketHandler.getEntries()
        val maxPages = entries.size / 45
        try {
            openGui(context, entries, 0, maxPages)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
        return 1
    }

    private fun openGui(context: CommandContext<ServerCommandSource>, entries: List<StoredMarketEntry>, page: Int, maxPage: Int) {
        val player = context.source.player
        player.closeContainer()

        val gui = ChestGui.builder {
            player(context.source.player)
            emptyIcon(ItemStack(Items.BLACK_STAINED_GLASS_PANE))

            // Add all market buttons
            val itemsOnDisplay = entries.subList(page * 45, Integer.min((page+1)*45, entries.size))
            itemsOnDisplay.forEachIndexed { index, storedMarketEntry ->
                button(index % 9, index / 9, storedMarketEntry.item) {
                    buyItem(player, storedMarketEntry)
                }
            }

            // Navigation
            for (i in 0 until 9) {
                // Filler
                button(i, 5, ItemStack(Items.GREEN_STAINED_GLASS_PANE)) { }
            }

            if (maxPage != 0) {
                button(0, 5, ItemStack(Items.BLUE_STAINED_GLASS_PANE).setCustomName(LiteralText("Previous page"))) {
                    val prevPage = if (page == 0) maxPage else page - 1
                    try {
                        println("Navigating to $prevPage")
                        openGui(context, entries, prevPage, maxPage)
                    } catch (e: StackOverflowError) {
                        player.closeContainer()
                    }
                }

                button(8, 5, ItemStack(Items.BLUE_STAINED_GLASS_PANE).setCustomName(LiteralText("Next page"))) {
                    val nextPage = if (page == maxPage) 0 else page + 1
                    try {
                        println("Navigating to $nextPage")
                        openGui(context, entries, nextPage, maxPage)
                    } catch (e: StackOverflowError) {
                        player.closeContainer()
                    }
                }
            }
        }

        player.networkHandler.sendPacket(
                OpenContainerS2CPacket(gui.syncId, gui.type, LiteralText("Market")))
        gui.addListener(player)
        player.container = gui
    }

    private fun buyItem(player: ServerPlayerEntity, entry: StoredMarketEntry) {
        val balance = balanceHandler.getUser(player.uuid).balance

        // Check if user has enough money
        if (balance < entry.price) {
            player.addChatMessage(LiteralText("Not enough money!"), false)
        } else {
            // Check if still present
            if (marketHandler.getEntries().contains(entry)) {
                marketHandler.deleteEntry(entry)
                balanceHandler.modifyUser(player.uuid) {
                    it.balance -= entry.price
                    it
                }
                balanceHandler.modifyUser(entry.uuid) {
                    it.balance += entry.price
                    it
                }

                // Remove listing data
                val item = entry.item
                val tag = item.tag!!
                val display = tag.getCompound("display")
                val lore = display.getList("Lore", NbtType.STRING) as ListTag

                lore.removeAt(3)
                lore.removeAt(2)
                lore.removeAt(1)
                lore.removeAt(0)

                display.put("Lore", lore)
                tag.put("display", display)
                item.tag = tag

                // Give item
                if (player.giveItemStack(item)) {
                    // Unable to insert
                    ItemScatterer.spawn(player.world, player.x, player.y, player.z, item)
                }

                player.addChatMessage(
                        LiteralText("Successfully purchased item!"),
                        false)
                player.server.playerManager.getPlayer(entry.uuid)?.addChatMessage(
                        LiteralText("${player.entityName} purchased one of your items!"),
                        false)

            } else {
                player.addChatMessage(LiteralText("Item no longer available"), false)
            }
        }
    }
}
