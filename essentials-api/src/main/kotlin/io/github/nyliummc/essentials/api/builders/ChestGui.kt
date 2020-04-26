package io.github.nyliummc.essentials.api.builders

import io.github.nyliummc.essentials.api.EssentialsMod
import net.minecraft.container.Container
import net.minecraft.container.SlotActionType
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity

interface ChestGui {
    companion object {
        @JvmStatic
        fun builder(callback: Builder.() -> Unit): Container {
            val builder = EssentialsMod.instance!!.registry.getBuilder(Builder::class.java)
            callback(builder)
            return builder.build()
        }

        @JvmStatic
        fun factory(callback: Builder.() -> Unit): (ServerPlayerEntity) -> Container {
            val builder = EssentialsMod.instance!!.registry.getBuilder(Builder::class.java)
            callback(builder)
            return {
                builder.player(it)
                builder.build()
            }
        }
    }

    interface Builder {
        fun player(player: ServerPlayerEntity)
        fun button(x: Int, y: Int, icon: ItemStack, clickCallback: (SlotActionType) -> Unit)
        fun emptyIcon(icon: ItemStack)

        @Deprecated("Used internally, do not use.")
        fun build(): Container
    }
}
