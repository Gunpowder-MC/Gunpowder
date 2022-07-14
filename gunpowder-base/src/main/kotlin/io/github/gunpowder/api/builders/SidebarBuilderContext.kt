package io.github.gunpowder.api.builders

import io.github.gunpowder.api.types.TimeUnit
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Formatting
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit

interface SidebarBuilderContext {
    fun build(block: SidebarBuilder.() -> Unit)

    interface SidebarBuilder {
        fun bind(player: ServerPlayerEntity)

        fun title(title: String, formatting: Formatting? = null)
        fun line(text: String, formatting: Formatting? = null)
        fun line(text: () -> String, formatting: Formatting? = null)

        fun scrolling(size: Int = -1, block: ScrollingSidebarBuilder.() -> Unit)
        fun tick(duration: Long, unit: TemporalUnit = ChronoUnit.SECONDS)
        fun tick(unit: TimeUnit)

        fun removeAfter(duration: Long, unit: TemporalUnit = ChronoUnit.SECONDS)
        fun removeAfter(unit: TimeUnit)
    }

    interface ScrollingSidebarBuilder {
        fun line(text: String, formatting: Formatting? = null)
        fun line(text: () -> String, formatting: Formatting? = null)
    }
}
