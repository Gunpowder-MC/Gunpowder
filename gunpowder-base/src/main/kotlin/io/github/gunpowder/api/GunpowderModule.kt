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

    // Always called
    open fun onLoad() { }

    // Called when the module is set to enabled (or on server start if it was already enabled)
    open fun onEnable() { }

    // Called when the module is set to disabled
    open fun onDisable() { }

    // Called on /reload, ONLY if the module is enabled
    open fun onReload() { }

    private object Builders : KoinComponent {
        val commands by inject<CommandBuilderContext>()
    }

    // Commands registered this way are automatically disabled when the module is disabled
    fun commands(block: CommandBuilderContext.() -> Unit) {
        block(Builders.commands)
    }
}
