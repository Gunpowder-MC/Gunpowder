package io.github.gunpowder.api.ext

import io.github.gunpowder.api.builders.SidebarBuilderContext
import io.github.gunpowder.api.types.ChestGUI
import me.lucko.fabric.api.permissions.v0.Options
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface ChestGUIOpener {
    fun open(player: ServerPlayerEntity, gui: ChestGUI)
}

private object SidebarAndGUIProvider : KoinComponent {
    val sidebar by inject<SidebarBuilderContext>()
    val gui by inject<ChestGUIOpener>()
}

fun ServerPlayerEntity.showSidebar(block: SidebarBuilderContext.SidebarBuilder.() -> Unit) {
    SidebarAndGUIProvider.sidebar.build {
        bind(this@showSidebar)
        block()
    }
}

fun ServerPlayerEntity.openGUI(gui: ChestGUI) {
    SidebarAndGUIProvider.gui.open(this, gui)
}

fun <T : Any> ServerPlayerEntity.getPermissionValue(permission: String, default: T, transform: (String) -> T) : T {
    return Options.get(this, permission, default, transform)
}

fun ServerPlayerEntity.getPermissionValue(permission: String, default: String): String {
    return getPermissionValue(permission, default) { it }
}
