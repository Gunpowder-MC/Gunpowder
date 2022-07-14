package io.github.gunpowder.mod.platform

import io.github.gunpowder.mod.GunpowderModImpl
import io.github.gunpowder.mod.database.ServerDatabase
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.MinecraftServer

class GunpowderServerMod : GunpowderModImpl(::ServerDatabase), DedicatedServerModInitializer {
    override val isClient = false

    @Suppress("DEPRECATION")
    override val server by lazy {
        FabricLoader.getInstance().gameInstance as MinecraftServer
    }

    override fun onInitializeServer() {
        onInitialize()
    }
}
