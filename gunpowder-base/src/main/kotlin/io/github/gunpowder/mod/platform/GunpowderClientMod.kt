package io.github.gunpowder.mod.platform

import io.github.gunpowder.mod.GunpowderModImpl
import io.github.gunpowder.mod.database.ClientDatabase
import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.MinecraftClient
import net.minecraft.server.MinecraftServer

class GunpowderClientMod : GunpowderModImpl(::ClientDatabase), ClientModInitializer {
    override val isClient = true

    private val client by lazy {
        MinecraftClient.getInstance()
    }

    override val server: MinecraftServer
        get() = client.server ?: throw IllegalStateException("Server is not available.")

    override fun onInitializeClient() {
        onInitialize()
    }
}
