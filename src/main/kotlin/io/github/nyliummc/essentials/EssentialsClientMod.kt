package io.github.nyliummc.essentials

import io.github.nyliummc.essentials.AbstractEssentialsMod
import io.github.nyliummc.essentials.api.EssentialsMod
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.server.MinecraftServer

/**
 * An implementation of the mod for clients which run an IntegratedServer.
 */
@Environment(EnvType.CLIENT)
class EssentialsClientMod : AbstractEssentialsMod(), ClientModInitializer {
    private val client by lazy {
        FabricLoader.getInstance().gameInstance as MinecraftClient
    }
    override val isClient = true
    override val server: MinecraftServer
        get() {
            return client.server ?: throw IllegalArgumentException("Server is not available.")
        }

    override fun onInitializeClient() {
        this.initialize()
    }
}