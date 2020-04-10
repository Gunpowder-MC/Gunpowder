package io.github.nyliummc.essentials.impl

import net.minecraft.server.MinecraftServer

abstract class AbstractEssentialsMod internal constructor() {
    protected abstract val server: MinecraftServer?
    fun initialize() {
        EssentialsImpl.instance
    }

    companion object {
        private var instance: AbstractEssentialsMod? = null
        val currentServer: MinecraftServer?
            get() = instance!!.server
    }

    init {
        instance = this
    }
}