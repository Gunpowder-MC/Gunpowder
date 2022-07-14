package io.github.gunpowder.gui

import io.github.gunpowder.api.GunpowderScheduler
import io.github.gunpowder.api.types.ChestGUI
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.SimpleInventory
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.screen.ScreenHandlerListener
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.server.network.ServerPlayerEntity
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.concurrent.thread

class ChestGUIContainer(syncId: Int, playerInv: PlayerInventory, private val gui: ChestGUI) : GenericContainerScreenHandler(getType(gui.rows),  syncId, playerInv, SimpleInventory(9 * gui.rows), gui.rows), KoinComponent {
    private val player = playerInv.player
    private var opened = false
    private val scheduler by inject<GunpowderScheduler>()

    init {
        updateInventory()
    }

    private fun tick() {
        if (gui.tick()) {
            updateInventory()
        }
    }

    private fun updateInventory() {
        for (i in 0 until (9 * rows)) {
            val x = i % 9
            val y = i / 9
            val stack = gui.buttons.firstOrNull { it.x == x && it.y == y }?.icon ?: gui.empty
            inventory.setStack(i, stack)
        }
    }

    override fun canUse(player: PlayerEntity?): Boolean {
        return true
    }

    override fun addListener(listener: ScreenHandlerListener) {
        gui.open(player as ServerPlayerEntity)
        opened = true
        scheduler.schedule(interval=gui.tickInterval, check =  { opened }) {
            tick()
        }
        super.addListener(listener)
    }

    override fun onSlotClick(slotIndex: Int, button: Int, actionType: SlotActionType, player: PlayerEntity) {
        val x = slotIndex % 9
        val y = slotIndex / 9
        val btn = gui.buttons.firstOrNull { it.x == x && it.y == y }
        btn?.onClick?.invoke(actionType)
    }

    override fun close(player: PlayerEntity) {
        gui.close()
        opened = false
        super.close(player)
    }

    companion object {
        fun getType(rows: Int): ScreenHandlerType<GenericContainerScreenHandler> {
            return when (rows) {
                1 -> ScreenHandlerType.GENERIC_9X1
                2 -> ScreenHandlerType.GENERIC_9X2
                3 -> ScreenHandlerType.GENERIC_9X3
                4 -> ScreenHandlerType.GENERIC_9X4
                5 -> ScreenHandlerType.GENERIC_9X5
                6 -> ScreenHandlerType.GENERIC_9X6
                else -> throw IllegalArgumentException("ChestGUI can only have 1-6 rows")
            }
        }
    }
}
