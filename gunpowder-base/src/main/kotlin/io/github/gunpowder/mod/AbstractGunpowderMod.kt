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
import io.github.gunpowder.api.GunpowderDatabase
import io.github.gunpowder.api.GunpowderMod
import io.github.gunpowder.api.GunpowderModule
import io.github.gunpowder.api.registerConfig
import io.github.gunpowder.configs.GunpowderConfig
import io.github.gunpowder.entities.*
import io.github.gunpowder.entities.database.AbstractDatabase
import io.github.gunpowder.entities.database.GunpowderClientDatabase
import io.github.gunpowder.entities.database.GunpowderServerDatabase
import io.github.gunpowder.injection.AbstractModule
import net.fabricmc.loader.api.FabricLoader
import org.apache.logging.log4j.LogManager

abstract class AbstractGunpowderMod : GunpowderMod {
    val module = "gunpowder:modules"
    override val logger = LogManager.getLogger(GunpowderMod::class.java)
    override val registry = GunpowderRegistry
    override lateinit var database: AbstractDatabase
    override val dimensionManager = DimensionManager
    override val languageEngine = LanguageHandler
    val injector: Injector

    init {
        injector = Guice.createInjector(this.createModule())
    }

    val modules = mutableListOf<GunpowderModule>()

    fun reload() {
        modules.forEach {
            it.reload()
        }
    }

    fun initialize() {
        logger.info("Starting Gunpowder")
        database = if (this.isClient) GunpowderClientDatabase else GunpowderServerDatabase
        GunpowderRegistry.registerBuiltin()
        LanguageHandler.get("en_us")
        logger.info("Loading modules")

        val entrypoints = FabricLoader.getInstance().getEntrypointContainers(module, GunpowderModule::class.java).sortedBy { it.entrypoint.priority }

        registry.registerConfig<GunpowderConfig>("gunpowder.yaml", "gunpowder.yaml")

        entrypoints.forEach {
            val module = it.entrypoint
            logger.info("Loading module ${module.name}, provided by ${it.provider.metadata.id}")
            module.registerConfigs()
            module.registerEvents()
            module.registerComponents()
            module.registerTables()
            module.registerCommands()
            module.onInitialize()
            modules.add(module)
        }
    }

    abstract fun createModule(): AbstractModule
}
