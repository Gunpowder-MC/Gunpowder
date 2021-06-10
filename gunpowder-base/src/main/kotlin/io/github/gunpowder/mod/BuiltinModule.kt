package io.github.gunpowder.mod

import io.github.gunpowder.api.GunpowderMod
import io.github.gunpowder.api.GunpowderModule
import io.github.gunpowder.api.builders.SignType
import io.github.gunpowder.api.components.bind
import io.github.gunpowder.api.components.with
import io.github.gunpowder.api.exposed.PlayerTable
import io.github.gunpowder.api.registerConfig
import io.github.gunpowder.commands.InfoCommand
import io.github.gunpowder.configs.GunpowderConfig
import io.github.gunpowder.entities.GunpowderDatabase
import io.github.gunpowder.entities.builtin.SignTypeComponent
import io.github.gunpowder.entities.mc.ChestGuiContainer
import io.github.gunpowder.events.BlockPreBreakCallback
import io.github.gunpowder.mixin.cast.SignBlockEntityMixinCast_Base
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.block.AbstractSignBlock
import net.minecraft.block.entity.SignBlockEntity
import net.minecraft.text.LiteralText
import net.minecraft.util.ActionResult

object BuiltinModule : GunpowderModule {
    override val name = "base"
    override val toggleable = false

    val gunpowder: GunpowderMod
        get() = GunpowderMod.instance

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
    }

    override fun registerComponents() {
        SignBlockEntity::class.bind<SignTypeComponent>()
    }

    override fun registerCommands() {
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

        ServerLifecycleEvents.SERVER_STOPPED.register { server ->
            // Disable DB, unregister everything except commands
            GunpowderDatabase.disconnect()
        }

        ServerTickEvents.START_WORLD_TICK.register(ServerTickEvents.StartWorldTick {
            if (counter++ == 19) {
                counter = 0
                guis.forEach(ChestGuiContainer::update)
            }
        })

//        if (GunpowderMod.instance.isClient) {
//            ServerLifecycleEvents.SERVER_STARTED.register {
//                GunpowderDatabase.loadDatabase()
//            }
//        }
    }
}
