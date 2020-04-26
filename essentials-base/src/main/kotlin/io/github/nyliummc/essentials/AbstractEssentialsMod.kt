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

package io.github.nyliummc.essentials

import io.github.nyliummc.essentials.api.EssentialsMod
import io.github.nyliummc.essentials.api.EssentialsModule
import io.github.nyliummc.essentials.entities.EssentialsDatabase
import io.github.nyliummc.essentials.entities.EssentialsRegistry
import net.fabricmc.fabric.api.event.server.ServerStartCallback
import net.fabricmc.fabric.api.event.server.ServerStopCallback
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.entrypoint.EntrypointContainer

abstract class AbstractEssentialsMod : EssentialsMod {
    val MODULE = "essentials:modules"

    override val registry = EssentialsRegistry
    override val database = EssentialsDatabase

    lateinit var entrypoints: List<EntrypointContainer<EssentialsModule>>

    fun initialize() {
        EssentialsMod.instance = this
        registry.registerBuiltin()
        entrypoints = FabricLoader.getInstance().getEntrypointContainers(MODULE, EssentialsModule::class.java)

        entrypoints.forEach {
            it.entrypoint.registerCommands()
        }

        ServerStartCallback.EVENT.register(ServerStartCallback { server ->
            database.loadDatabase()

            entrypoints.forEach {
                // TODO: Dependency inject essentials field

                // Register non-commands
                it.entrypoint.onInitialize()
            }
        })

        ServerStopCallback.EVENT.register(ServerStopCallback { server ->
            // Disable DB, unregister everything except commands
            database.disconnect()
        })
    }
}
