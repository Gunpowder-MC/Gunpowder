package io.github.gunpowder.api

import io.github.gunpowder.api.builders.CommandBuilderContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

abstract class GunpowderModule : KoinComponent {
    abstract val name: String
    open val priority: Int = 1000

    var enabled: Boolean = false
        internal set
    open val toggleable = false

    val gunpowder by inject<GunpowderMod>()
    val database by inject<GunpowderDatabase>()
    val registry by inject<GunpowderRegistry>()
    val scheduler by inject<GunpowderScheduler>()

    open fun onLoad() { }
    open fun onEnable() { }
    open fun onDisable() { }

    private object Builders : KoinComponent {
        val commands by inject<CommandBuilderContext>()
    }

    fun commands(block: CommandBuilderContext.() -> Unit) {
        block(Builders.commands)
    }
}
