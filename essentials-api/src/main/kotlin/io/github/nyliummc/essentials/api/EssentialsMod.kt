package io.github.nyliummc.essentials.api

import net.minecraft.server.MinecraftServer

interface EssentialsMod {
    val server: MinecraftServer
    val registry: EssentialsRegistry

    // TODO: Cleaner way to do this
    companion object {
        var instance: EssentialsMod? = null
    }
}
