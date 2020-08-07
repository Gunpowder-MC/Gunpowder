/*
 * MIT License
 *
 * Copyright (c) GunpowderMC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.gunpowder.mod

import com.google.inject.Guice
import com.google.inject.Injector
import io.github.gunpowder.api.GunpowderMod
import io.github.gunpowder.api.GunpowderModule
import io.github.gunpowder.entities.DimensionManager
import io.github.gunpowder.entities.GunpowderDatabase
import io.github.gunpowder.entities.GunpowderRegistry
import io.github.gunpowder.entities.LanguageHack
import io.github.gunpowder.injection.AbstractModule
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import org.apache.logging.log4j.LogManager

abstract class AbstractGunpowderMod : GunpowderMod {
    val module = "gunpowder:modules"
    override val logger = LogManager.getLogger(GunpowderMod::class.java)
    override val registry = GunpowderRegistry
    override val database = GunpowderDatabase
    override val dimensionManager = DimensionManager
    val injector: Injector

    init {
        injector = Guice.createInjector(this.createModule())
    }

    var modules: MutableList<GunpowderModule> = mutableListOf()

    fun initialize() {
        FabricLoader.getInstance().allMods.filter { itt -> itt.metadata.depends.any { it.modId == "gunpowder-base" } }.forEach { LanguageHack.activate(it.metadata.name) }

        logger.info("Starting Gunpowder")
        registry.registerBuiltin()
        logger.info("Loading modules")

        val entrypoints = FabricLoader.getInstance().getEntrypointContainers(module, GunpowderModule::class.java)

        // Register events before registering commands
        // in case of a RegisterCommandEvent or something
        entrypoints.forEach {
            it.entrypoint.registerEvents()
        }

        entrypoints.forEach {
            val module = it.entrypoint
            modules.add(module)
            logger.info("Loaded module ${module.name}, provided by ${it.provider.metadata.id}")
            // We need to register configs as early as possible. The actual reloading of configs to handle per world settings can be done after the server has stopped for singleplayer
            // This is due to LiteralTextMixin_Chat accessing the config during a Resource reload.
            // Thereby accessing the gunpowder instance BEFORE the server start callbacks have been fired
            module.registerConfigs()
            module.registerCommands()
        }

        // TODO: Look into cleanup so we can turn this into internal method references
        ServerLifecycleEvents.SERVER_STARTED.register(ServerLifecycleEvents.ServerStarted { server ->
            database.loadDatabase()

            modules.forEach {
                // Register non-commands
                it.onInitialize()
            }
        })

        ServerLifecycleEvents.SERVER_STOPPED.register(ServerLifecycleEvents.ServerStopped { server ->
            // Disable DB, unregister everything except commands
            database.disconnect()
        })
    }

    abstract fun createModule(): AbstractModule
}
