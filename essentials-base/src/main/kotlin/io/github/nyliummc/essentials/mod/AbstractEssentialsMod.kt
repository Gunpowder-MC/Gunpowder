/*
 * MIT License
 *
 * Copyright (c) NyliumMC
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

package io.github.nyliummc.essentials.mod

import com.google.inject.Guice
import com.google.inject.Injector
import io.github.nyliummc.essentials.api.EssentialsMod
import io.github.nyliummc.essentials.api.EssentialsModule
import io.github.nyliummc.essentials.api.module.ModuleEntrypoint
import io.github.nyliummc.essentials.entities.EssentialsDatabase
import io.github.nyliummc.essentials.entities.EssentialsRegistry
import io.github.nyliummc.essentials.injection.AbstractModule
import net.fabricmc.fabric.api.event.server.ServerStartCallback
import net.fabricmc.fabric.api.event.server.ServerStopCallback
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.entrypoint.EntrypointContainer
import org.apache.logging.log4j.LogManager

abstract class AbstractEssentialsMod : EssentialsMod {
    val module = "essentials:modules"
    val logger = LogManager.getLogger(EssentialsMod::class.java)

    override val registry = EssentialsRegistry
    override val database = EssentialsDatabase
    val injector: Injector

    init {
        injector = Guice.createInjector(this.createModule())
    }

    var modules: MutableList<EssentialsModule> = mutableListOf()

    fun initialize() {
        logger.info("Starting Essentials")
        registry.registerBuiltin()
        logger.info("Loading modules")

        val entrypoints = FabricLoader.getInstance().getEntrypointContainers(module, ModuleEntrypoint::class.java)

        entrypoints.forEach {
            val module = it.entrypoint.createModule(this.injector)
            modules.add(module)
            logger.info("Loaded module ${module.name}, provided by ${it.provider.metadata.id}")
            // We need to register configs as early as possible. The actual reloading of configs to handle per world settings can be done after the server has stopped for singleplayer
            // This is due to LiteralTextMixin_Chat accessing the config during a Resource reload.
            // Thereby accessing the essentials instance BEFORE the server start callbacks have been fired
            // TODO fixme: Module magic and crap since J9 no likey Guice injections, this will require switching back to JVM 11 runtime to test
            module.registerConfigs()
            module.registerCommands()
        }

        // TODO: Look into cleanup so we can turn this into internal method references
        ServerStartCallback.EVENT.register(ServerStartCallback { server ->
            database.loadDatabase()

            modules.forEach {
                // TODO: Dependency inject essentials field onto the modules

                // Register non-commands
                it.onInitialize()
            }
        })

        ServerStopCallback.EVENT.register(ServerStopCallback { server ->
            // Disable DB, unregister everything except commands
            database.disconnect()
        })
    }

    abstract fun createModule(): AbstractModule
}
