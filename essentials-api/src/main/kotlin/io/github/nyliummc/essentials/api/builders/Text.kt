package io.github.nyliummc.essentials.api.builders

import io.github.nyliummc.essentials.api.EssentialsMod
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.util.Formatting
import net.minecraft.text.Text as MCText


interface Text {
    companion object fun builder(callback: Builder.() -> MCText): MCText {
        val builder = EssentialsMod.instance!!.registry.getBuilder(Builder::class.java)
        callback(builder)
        return builder.build()
    }

    interface Builder {
        fun text(text: String)
        fun text(text: MCText)
        fun text(text: String, callback: StyleBuilder.()->Unit)
        fun text(text: MCText, callback: StyleBuilder.()->Unit)

        @Deprecated("Used internally, do not use.")
        fun build(): MCText
    }

    interface StyleBuilder {
        fun onClickCommand(command: String)
        fun onHoverText(tooltip: String)
        fun color(color: Formatting)
        fun bold()
        fun italic()
        fun strikethrough()
        fun underlined()
        fun obfuscated()
        fun onClickEvent(event: ClickEvent)
        fun onHoverEvent(event: HoverEvent)
    }
}