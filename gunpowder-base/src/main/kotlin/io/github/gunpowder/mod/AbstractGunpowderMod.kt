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
import io.github.gunpowder.api.LanguageUtil
import io.github.gunpowder.entities.*
import io.github.gunpowder.injection.AbstractModule
import net.fabricmc.loader.api.FabricLoader
import org.apache.logging.log4j.LogManager

abstract class AbstractGunpowderMod : GunpowderMod {
    val module = "gunpowder:modules"
    override val logger = LogManager.getLogger(GunpowderMod::class.java)
    override val registry = GunpowderRegistry
    override val database = GunpowderDatabase
    override val dimensionManager = DimensionManager
    override val languageEngine = LanguageHandler
    val injector: Injector

    init {
        injector = Guice.createInjector(this.createModule())
    }

    var modules: MutableList<GunpowderModule> = mutableListOf()

    fun reload() {
        modules.forEach {
            it.reload()
        }
    }

    fun initialize() {
        logger.info("Starting Gunpowder")
        GunpowderRegistry.registerBuiltin()
        GunpowderDatabase.loadDatabase()
        GunpowderEvents.init()
        LanguageHandler.get("en_us")
        logger.info("Loading modules")

        val entrypoints = FabricLoader.getInstance().getEntrypointContainers(module, GunpowderModule::class.java).sortedBy { it.entrypoint.priority }

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
            module.onInitialize()  // Moved this to earlier than SERVER_STARTED since loading tags may need custom data
        }
    }

    abstract fun createModule(): AbstractModule
}
