package io.github.nyliummc.essentials.api.permission

import net.minecraft.util.Identifier
import java.util.*

/**
 * Used as the "default" permissions engine for servers where a full permissions implementation does not exist.
 * This system is configurable where the "vanilla" permission levels can be set by a config.
 */
@Deprecated("Please replace with a PermissionProvider from Fabric API in the future")
interface VanillaPermissionEngine {
    /**
     * Gets the vanilla permission level this permission is registered under.
     * @return The vanilla permission level this permission will fall back to.
     * If this permission does not have a registration then it will return empty.
     */
    fun getVanillaPermissionLevel(permission: Identifier): OptionalInt
}