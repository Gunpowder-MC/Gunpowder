package io.github.nyliummc.essentials.impl

import net.minecraft.server.MinecraftServer

abstract class AbstractEssentialsMod() {
    protected abstract val server: MinecraftServer?
    fun initialize() {
        EssentialsImpl.instance
        // TODO: Testing
        println("This works I guess")
        // TODO: Testing
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