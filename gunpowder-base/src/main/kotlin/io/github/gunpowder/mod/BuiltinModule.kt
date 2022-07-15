package io.github.gunpowder.mod

import io.github.gunpowder.api.GunpowderModule
import io.github.gunpowder.api.events.DatabaseEvents
import io.github.gunpowder.api.tables.PlayerTable
import io.github.gunpowder.mixinterfaces.SignTypeBE
import io.github.gunpowder.mod.client.RegisteredArgumentTypesC2SPacket
import io.github.gunpowder.mod.commands.GunpowderCommand
import io.github.gunpowder.mod.commands.HelpCommand
import net.fabricmc.api.EnvType
import net.fabricmc.fabric.api.client.networking.v1.C2SPlayChannelEvents
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.SignBlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import org.jetbrains.exposed.sql.SchemaUtils

object BuiltinModule : GunpowderModule() {
    override val name = "base"

    internal val configCache = mutableMapOf<String, Any>()

    override fun onLoad() {
        commands {
            HelpCommand.build()
            GunpowderCommand.build()
        }

        DatabaseEvents.DATABASE_READY.register {
            database.transaction {
                SchemaUtils.createMissingTablesAndColumns(
                    PlayerTable
                )
            }
        }

        CommandRegistrationCallback.EVENT.register { dispatcher, _->
            HelpCommand.dispatcher = dispatcher
        }

        PlayerBlockBreakEvents.BEFORE.register { _: World, playerEntity: PlayerEntity, _: BlockPos, _: BlockState, blockEntity: BlockEntity? ->
            val sbe = (blockEntity as? SignTypeBE)
            if (enabled && blockEntity != null && blockEntity is SignBlockEntity && sbe!!.isCustom) {
                playerEntity.hasPermissionLevel(4) || sbe.isCreator(playerEntity)
            } else {
                true
            }
        }

        ServerPlayerEvents.COPY_FROM.register { old: ServerPlayerEntity, new: ServerPlayerEntity, _: Boolean ->
            GunpowderRegistryImpl.setKnownArgumentTypes(new, GunpowderRegistryImpl.getKnownArgumentTypes(old))
        }

        if (FabricLoader.getInstance().isModLoaded("fabric-networking-v0")) {
            RegisteredArgumentTypesC2SPacket.register()
            if (FabricLoader.getInstance().environmentType == EnvType.CLIENT) {
                C2SPlayChannelEvents.REGISTER.register(C2SPlayChannelEvents.Register { _: ClientPlayNetworkHandler?, sender: PacketSender, client: MinecraftClient, channels: List<Identifier?> ->
                    if (channels.contains(RegisteredArgumentTypesC2SPacket.ID)) {
                        client.execute {
                            RegisteredArgumentTypesC2SPacket(GunpowderRegistryImpl.satIds).sendTo(sender)
                        }
                    }
                })
            }
        }
    }

    override fun onReload() {
        configCache.clear()
    }
}
