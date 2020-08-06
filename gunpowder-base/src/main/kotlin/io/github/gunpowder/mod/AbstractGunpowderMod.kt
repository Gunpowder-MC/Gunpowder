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
import io.github.gunpowder.api.builders.Command
import io.github.gunpowder.entities.DimensionManager
import io.github.gunpowder.entities.GunpowderDatabase
import io.github.gunpowder.entities.GunpowderRegistry
import io.github.gunpowder.entities.LanguageHack
import io.github.gunpowder.events.PlayerTeleportCallback
import io.github.gunpowder.injection.AbstractModule
import io.github.gunpowder.mixin.cast.SyncPlayer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket
import net.minecraft.tag.BlockTags
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryKey
import net.minecraft.world.GameRules
import net.minecraft.world.biome.source.BiomeAccess
import net.minecraft.world.dimension.DimensionType
import net.minecraft.world.gen.chunk.FlatChunkGenerator
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig
import org.apache.logging.log4j.LogManager
import java.util.*

abstract class AbstractGunpowderMod : GunpowderMod {
    val module = "gunpowder:modules"
    override val logger = LogManager.getLogger(GunpowderMod::class.java)
    override val registry = GunpowderRegistry
    override val database = GunpowderDatabase
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

        registry.registerCommand {
            Command.builder(it) {
                command("testdim") {
                    executes {
                        println("Registering custom dim")
                        val dtype = RegistryKey.of(Registry.DIMENSION_TYPE_KEY, Identifier("gunpowder:custom"))
                        DimensionManager.addDimensionType(dtype, DimensionType(OptionalLong.of(2400L), true, false, false, false, false, true, true, true, false, 256, BlockTags.INFINIBURN_OVERWORLD.id, 0.0f))
                        DimensionManager.addWorld(RegistryKey.of(Registry.DIMENSION, Identifier("gunpowder:abc")), dtype, FlatChunkGenerator(FlatChunkGeneratorConfig.getDefaultConfig()))
                        1
                    }
                }
            }
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

        PlayerTeleportCallback.EVENT.register(PlayerTeleportCallback { player, request ->
            if (player.world.dimensionRegistryKey.value != request.dimension) {
                if ((player as SyncPlayer).needsSync()) {
                    player.networkHandler.sendPacket(GameJoinS2CPacket(
                            player.entityId, player.interactionManager.gameMode, player.interactionManager.method_30119(),
                            BiomeAccess.hashSeed(player.serverWorld.seed), player.world.levelProperties.isHardcore, DimensionManager.server.worldRegistryKeys,
                            DimensionManager.server.playerManager.field_24626, player.world.dimensionRegistryKey, player.world.registryKey,
                            DimensionManager.server.playerManager.maxPlayerCount, DimensionManager.server.playerManager.viewDistance,
                            player.world.gameRules.getBoolean(GameRules.REDUCED_DEBUG_INFO),
                            !player.world.gameRules.getBoolean(GameRules.DO_IMMEDIATE_RESPAWN),
                            player.world.isDebugWorld, player.serverWorld.isFlat))
                    player.networkHandler.sendPacket(CloseScreenS2CPacket())
                    player.setNeedsSync(false)
                }
            }
        })
    }

    abstract fun createModule(): AbstractModule
}
