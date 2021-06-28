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

import io.github.gunpowder.api.GunpowderDatabase
import io.github.gunpowder.api.GunpowderMod
import io.github.gunpowder.api.GunpowderModule
import io.github.gunpowder.api.components.bind
import io.github.gunpowder.api.components.with
import io.github.gunpowder.api.exposed.PlayerTable
import io.github.gunpowder.commands.HelpCommand
import io.github.gunpowder.commands.InfoCommand
import io.github.gunpowder.entities.database.GunpowderClientDatabase
import io.github.gunpowder.entities.database.GunpowderServerDatabase
import io.github.gunpowder.entities.arguments.ServerArgumentTypes
import io.github.gunpowder.entities.builtin.PlayerArgumentComponent
import io.github.gunpowder.entities.builtin.SignTypeComponent
import io.github.gunpowder.entities.database.AbstractDatabase
import io.github.gunpowder.entities.mc.ChestGuiContainer
import io.github.gunpowder.entities.mc.RegisteredArgumentTypesC2SPacket
import io.github.gunpowder.events.BlockPreBreakCallback
import net.fabricmc.api.EnvType
import net.fabricmc.fabric.api.client.networking.v1.C2SPlayChannelEvents
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.block.AbstractSignBlock
import net.minecraft.block.entity.SignBlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.Identifier


object BuiltinModule : GunpowderModule {
    override val name = "base"
    override val toggleable = false

    val gunpowder: GunpowderMod
        get() = GunpowderMod.instance
    val db: AbstractDatabase
        get() = GunpowderMod.instance.database as AbstractDatabase

    private var counter = 0
    val guis = mutableListOf<ChestGuiContainer>()

    override fun onInitialize() {
//        SIGN TYPE TEST
//        SignType.builder {
//            name("gp:hi")
//            onClicked { signBlockEntity, serverPlayerEntity ->
//                serverPlayerEntity.sendMessage(LiteralText("hi"), false)
//            }
//        }

        if (FabricLoader.getInstance().isModLoaded("fabric-networking-v0")) {
            RegisteredArgumentTypesC2SPacket.register()
            if (FabricLoader.getInstance().environmentType === EnvType.CLIENT) { // we need to send the packet
                C2SPlayChannelEvents.REGISTER.register(C2SPlayChannelEvents.Register { handler: ClientPlayNetworkHandler?, sender: PacketSender, client: MinecraftClient, channels: List<Identifier?> ->
                    if (channels.contains(RegisteredArgumentTypesC2SPacket.ID)) {
                        client.execute {
                            RegisteredArgumentTypesC2SPacket(ServerArgumentTypes.ids).sendTo(sender)
                        }
                    }
                })
            }
        }

    }

    override fun registerComponents() {
        ServerPlayerEntity::class.bind<PlayerArgumentComponent>()
        SignBlockEntity::class.bind<SignTypeComponent>()
    }

    override fun registerCommands() {
        gunpowder.registry.registerCommand(HelpCommand::register)
        gunpowder.registry.registerCommand(InfoCommand::register)

    }

    override fun registerTables() {
        gunpowder.registry.registerTable(PlayerTable)
    }

    override fun registerEvents() {
        BlockPreBreakCallback.EVENT.register { player, world, pos ->
            val state = world.getBlockState(pos)
            if (state.block is AbstractSignBlock) {
                val be = world.getBlockEntity(pos) as SignBlockEntity
                val comp = be.with<SignTypeComponent>()

                if (comp.custom &&
                    !player.hasPermissionLevel(4) &&
                    !comp.type.conditionEvent(be, player) &&
                    !comp.isCreator(player)) {
                    return@register ActionResult.FAIL;
                }
            }
            return@register ActionResult.PASS
        }

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register { minecraftServer, serverResourceManager, b ->
            val gp = (GunpowderMod.instance as AbstractGunpowderMod)
            gp.reload();
        }

        ServerLifecycleEvents.SERVER_STARTED.register { server ->
            db.loadDatabase()
        }

        ServerLifecycleEvents.SERVER_STOPPED.register { server ->
            db.disconnect()
        }

        ServerTickEvents.START_WORLD_TICK.register(ServerTickEvents.StartWorldTick {
            if (counter++ == 19) {
                counter = 0
                guis.forEach(ChestGuiContainer::update)
            }
        })

        ServerPlayerEvents.COPY_FROM.register { old, new, alive ->
            val oldArgs = old.with<PlayerArgumentComponent>()
            val newArgs = new.with<PlayerArgumentComponent>()
            newArgs.known.clear()
            newArgs.known.addAll(oldArgs.known)
        }
    }
}
