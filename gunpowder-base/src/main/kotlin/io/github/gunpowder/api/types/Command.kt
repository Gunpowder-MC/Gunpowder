package io.github.gunpowder.api.types

import io.github.gunpowder.api.builders.CommandBuilderContext

fun interface Command {
    fun CommandBuilderContext.build()
}
