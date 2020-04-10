package io.github.nyliummc.essentials.impl

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.server.MinecraftServer

@Environment(EnvType.CLIENT)
class EssentialsClientMod : AbstractEssentialsMod(), ClientModInitializer {
    private var client: MinecraftClient? = null
    override val server: MinecraftServer?
        get() {
            if (client == null) {
                client = FabricLoader.getInstance().gameInstance as MinecraftClient
            }
            if (client!!.server != null) {
                return client!!.server!!
            }
            throw IllegalArgumentException("Server is not available.")
        }

    override fun onInitializeClient() {
        initialize()
    }
}