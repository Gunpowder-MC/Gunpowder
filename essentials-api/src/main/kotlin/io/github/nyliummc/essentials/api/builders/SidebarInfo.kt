package io.github.nyliummc.essentials.api.builders

import io.github.nyliummc.essentials.api.EssentialsMod
import net.minecraft.server.network.ServerPlayerEntity
import java.util.function.Consumer

interface SidebarInfo {
    companion object {
        /**
         *
         */
        @JvmStatic
        fun factory(callback: Consumer<Builder>) = factory(callback::accept)
        fun factory(callback: Builder.() -> Unit): (ServerPlayerEntity) -> Unit {
            val builder = EssentialsMod.instance.registry.getBuilder(Builder::class.java)
            callback(builder)
            return builder::build
        }
    }

    interface Builder {
        fun addLine(text: Text)

        @Deprecated("Used internally, do not use.")
        fun build(player: ServerPlayerEntity)
    }
}
