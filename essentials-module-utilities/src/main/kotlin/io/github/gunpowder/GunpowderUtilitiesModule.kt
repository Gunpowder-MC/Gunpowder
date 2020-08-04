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

package io.github.gunpowder

import io.github.gunpowder.api.GunpowderMod
import io.github.gunpowder.api.GunpowderModule
import io.github.gunpowder.commands.*
import io.github.nyliummc.essentials.commands.*
import io.github.gunpowder.configs.UtilitiesConfig
import io.github.gunpowder.entities.TPSTracker
import io.github.gunpowder.mixin.cast.SleepSetter
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.MessageType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.LiteralText
import net.minecraft.util.Util
import net.minecraft.world.World
import kotlin.math.roundToInt
import kotlin.streams.toList


class GunpowderUtilitiesModule : GunpowderModule {
    override val name = "utilities"
    override val toggleable = true
    val gunpowder: GunpowderMod = GunpowderMod.instance

    val sleeping = mutableListOf<ServerPlayerEntity>()


    override fun registerCommands() {
        gunpowder.registry.registerCommand(EnderchestCommand::register)
        gunpowder.registry.registerCommand(FlightCommand::register)
        gunpowder.registry.registerCommand(GodCommand::register)
        gunpowder.registry.registerCommand(HatCommand::register)
        gunpowder.registry.registerCommand(HeadCommand::register)
        gunpowder.registry.registerCommand(HealCommand::register)
        gunpowder.registry.registerCommand(InvseeCommand::register)
        gunpowder.registry.registerCommand(PingCommand::register)
        // essentials.registry.registerCommand(SpeedCommand::register)  // See comments on object
        gunpowder.registry.registerCommand(TPSCommand::register)
        gunpowder.registry.registerCommand(TrashCommand::register)
        gunpowder.registry.registerCommand(VanishCommand::register);
        gunpowder.registry.registerCommand(WorkbenchCommand::register)
    }

    override fun registerConfigs() {
        gunpowder.registry.registerConfig("essentials-utilities.yaml", UtilitiesConfig::class.java, "essentials-utilities.yaml")
    }

    override fun registerEvents() {
        ServerTickEvents.END_WORLD_TICK.register(ServerTickEvents.EndWorldTick { world ->
            getTracker(world.registryKey.value.path).tick()
        })

        // Skip night by percentage
        ServerTickEvents.END_WORLD_TICK.register(ServerTickEvents.EndWorldTick { world ->
            if (world.isClient || world !== world.server.getWorld(World.OVERWORLD)) {
                return@EndWorldTick
            }

            val players = world.players
            val treshold = gunpowder.registry.getConfig(UtilitiesConfig::class.java).sleepPercentage

            players.removeIf { obj: PlayerEntity -> obj.isSpectator }

            if (players.isEmpty() || treshold <= 0) {
                (world as SleepSetter).setSleeping(false)
                return@EndWorldTick
            }

            val total = players.size.toDouble()
            val sleepingPlayers = players.stream().filter { it.isSleepingLongEnough }.toList()

            val sleepingAmount = sleepingPlayers.count().toDouble()
            val percentage = sleepingAmount / total
            val shouldSkip = percentage >= treshold

            sleepingPlayers.filter { !sleeping.contains(it) }.forEach {
                sleeping.add(it as ServerPlayerEntity)
                world.server.playerManager.broadcastChatMessage(
                        LiteralText("${it.displayName.asString()} is now sleeping. (${(percentage * 100).roundToInt()}%, ${(treshold * 100).roundToInt()} needed)"), MessageType.SYSTEM, Util.NIL_UUID)
            }

            (world as SleepSetter).setSleeping(shouldSkip)
            if (shouldSkip) {
                world.server.playerManager.broadcastChatMessage(LiteralText("Good morning!"), MessageType.SYSTEM, Util.NIL_UUID)
                sleeping.clear()
            }
        })
    }

    companion object {
        val tpsTrackers = mutableMapOf<String, TPSTracker>()

        @JvmStatic
        fun getTracker(name: String): TPSTracker {
            var c = tpsTrackers[name]
            if (c == null) {
                c = TPSTracker(name)
                tpsTrackers[name] = c
            }
            return c
        }
    }

}
