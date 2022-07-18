package io.github.gunpowder.api

import io.github.gunpowder.api.builders.CommandBuilderContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

abstract class GunpowderModule : KoinComponent {
    abstract val name: String
    open val priority: Int = 1000

    var enabled: Boolean = false
        internal set(value) {
            field = value
            if (field) {
                onEnable()
            } else {
                onDisable()
            }
        }
    open val toggleable = false

    val gunpowder by inject<GunpowderMod>()
    val database by inject<GunpowderDatabase>()
    val registry by inject<GunpowderRegistry>()
    val scheduler by inject<GunpowderScheduler>()

    // Always called
    open fun onLoad() { }

    // Called when:
    // - the module is set to enabled
    // - on server start if it wasn't previously disabled or if it's not toggleable
    open fun onEnable() { }

    // Called when:
    // - the module is set to disabled
    // - on server shutdown
    open fun onDisable() { }

    // Called on /reload, ONLY if the module is enabled
    open fun onReload() { }

    internal interface CommandBuilderContextProvider {
        fun get(module: GunpowderModule): CommandBuilderContext
    }

    private object Builders : KoinComponent {
        val commands by inject<CommandBuilderContextProvider>()
    }

    // Commands registered this way are automatically disabled when the module is disabled
    fun commands(block: CommandBuilderContext.() -> Unit) {
        block(Builders.commands.get(this))
    }
}
