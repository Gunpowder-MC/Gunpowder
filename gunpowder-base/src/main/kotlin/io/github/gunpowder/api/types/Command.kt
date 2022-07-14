package io.github.gunpowder.api.types

import io.github.gunpowder.api.GunpowderModule
import io.github.gunpowder.api.builders.CommandBuilderContext

fun interface Command {
    context(GunpowderModule, CommandBuilderContext)
    fun build()
}
