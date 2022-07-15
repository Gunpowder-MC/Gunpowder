package io.github.gunpowder.api

import net.minecraft.server.MinecraftServer

interface GunpowderMod {
    val isClient: Boolean
    val modules: List<GunpowderModule>
    val server: MinecraftServer
    val registry: GunpowderRegistry
    val database: GunpowderDatabase
    val scheduler: GunpowderScheduler
}
