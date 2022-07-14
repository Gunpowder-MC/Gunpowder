package io.github.gunpowder.gui

import io.github.gunpowder.api.ext.ChestGUIOpener
import io.github.gunpowder.api.types.ChestGUI
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

object ChestGUIOpenerImpl : ChestGUIOpener {
    override fun open(player: ServerPlayerEntity, gui: ChestGUI) {
        player.openHandledScreen(object : NamedScreenHandlerFactory {
            override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler {
                return ChestGUIContainer(syncId, inv, gui)
            }

            override fun getDisplayName(): Text {
                return gui.displayName
            }
        })
    }
}
