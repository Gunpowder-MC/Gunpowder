package io.github.nyliummc.essentials.impl.permission

import io.github.nyliummc.essentials.api.permission.VanillaPermissionEngine
import io.github.nyliummc.essentials.impl.EssentialsImpl
import net.minecraft.util.Identifier
import java.util.*

class VanillaPermissionEngineImpl(val essentialsImpl: EssentialsImpl) : VanillaPermissionEngine {
    override fun getVanillaPermissionLevel(permission: Identifier): OptionalInt {
        return OptionalInt.empty(); // TODO: Implement
    }
}
