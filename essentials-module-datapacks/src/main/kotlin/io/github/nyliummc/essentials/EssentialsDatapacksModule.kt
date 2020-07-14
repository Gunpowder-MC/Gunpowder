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

package io.github.nyliummc.essentials

import io.github.nyliummc.essentials.api.EssentialsMod
import io.github.nyliummc.essentials.api.EssentialsMod.Companion.instance
import io.github.nyliummc.essentials.api.EssentialsModule
import io.github.nyliummc.essentials.configs.DatapacksConfig
import io.github.nyliummc.essentials.events.PlayerDeathCallback
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtHelper
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.LiteralText
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.ItemScatterer

class EssentialsDatapacksModule : EssentialsModule {
    override val name = "datapacks"
    override val toggleable = true
    val essentials: EssentialsMod = EssentialsMod.instance

    override fun registerConfigs() {
        essentials.registry.registerConfig("essentials-datapacks.yaml", DatapacksConfig::class.java, "essentials-datapacks.yaml")
    }

    override fun registerEvents() {
        PlayerDeathCallback.EVENT.register(PlayerDeathCallback { player, source ->
            if (source.attacker is ServerPlayerEntity && source.attacker !== player) {
                // Killed by another player
                if (instance.registry.getConfig(DatapacksConfig::class.java).vanillaTweaks.playersDropHeads) {
                    // Dropping heads enabled
                    val stack = ItemStack(Items.PLAYER_HEAD)
                    var name: Text? = player.customName
                    if (name == null) {
                        name = player.name
                    }
                    stack.setCustomName(LiteralText(name!!.asString() + "'s Head").styled { style: Style -> style.withColor(Formatting.DARK_BLUE) })
                    stack.orCreateTag.put("SkullOwner", NbtHelper.fromGameProfile(CompoundTag(), player.gameProfile))
                    ItemScatterer.spawn(player.world, player.x, player.y, player.z, stack)
                }
            }
        })
    }

    override fun onInitialize() {

    }

}
