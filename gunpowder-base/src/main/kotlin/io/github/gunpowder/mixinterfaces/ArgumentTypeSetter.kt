package io.github.gunpowder.mixinterfaces

import com.mojang.brigadier.arguments.ArgumentType

interface ArgumentTypeSetter {
    fun setType(type: ArgumentType<*>)
}
