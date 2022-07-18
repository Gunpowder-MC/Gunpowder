package io.github.gunpowder.api.ext

import me.lucko.fabric.api.permissions.v0.Options
import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.server.command.ServerCommandSource


fun ServerCommandSource.hasPermission(permission: String, default: Boolean = false): Boolean {
    return Permissions.check(this, permission, default)
}

fun ServerCommandSource.hasPermission(permission: String, permissionLevel: Int): Boolean {
    return Permissions.check(this, permission, permissionLevel)
}

fun <T : Any> ServerCommandSource.getPermissionValue(permission: String, default: T, transform: (String) -> T) : T {
    return Options.get(this, permission, default, transform)
}

fun ServerCommandSource.getPermissionValue(permission: String, default: String): String {
    return getPermissionValue(permission, default) { it }
}
