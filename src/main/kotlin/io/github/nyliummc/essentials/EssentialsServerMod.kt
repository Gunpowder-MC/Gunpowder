package io.github.nyliummc.essentials

import io.github.nyliummc.essentials.AbstractEssentialsMod
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.MinecraftServer

/**
 * An implementation of the mod for DedicatedServers.
 */
class EssentialsServerMod : AbstractEssentialsMod(), DedicatedServerModInitializer {
    override val isClient = false

    override val server by lazy {
        FabricLoader.getInstance().gameInstance as MinecraftServer
    }

    override fun onInitializeServer() {
        this.initialize()
    }
}