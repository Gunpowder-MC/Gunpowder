package io.github.nyliummc.essentials.impl

import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.MinecraftServer

/**
 * An implementation of the mod for DedicatedServers.
 */
class EssentialsServerMod : AbstractEssentialsMod(), DedicatedServerModInitializer {
    override val server: MinecraftServer?
        get() = FabricLoader.getInstance().gameInstance as MinecraftServer

    override fun onInitializeServer() {
        initialize()
    }
}