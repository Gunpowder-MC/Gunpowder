package io.github.nyliummc.essentials

import io.github.nyliummc.essentials.api.EssentialsMod
import io.github.nyliummc.essentials.api.EssentialsModule
import io.github.nyliummc.essentials.entities.EssentialsDatabase
import io.github.nyliummc.essentials.entities.EssentialsRegistry
import net.fabricmc.loader.api.FabricLoader

abstract class AbstractEssentialsMod : EssentialsMod {
    val MODULE = "essentials:modules"

    override val registry = EssentialsRegistry
    override val database = EssentialsDatabase

    fun initialize() {
        EssentialsMod.instance = this

        val entrypoints = FabricLoader.getInstance().getEntrypointContainers(MODULE, EssentialsModule::class.java)
        entrypoints.forEach {
            // TODO: Dependency inject essentials field

            // Register stuff
            it.entrypoint.onInitialize()
        }
    }
}
