package io.github.nyliummc.essentials.entities.mc

import com.mojang.serialization.Lifecycle
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryKey
import net.minecraft.util.registry.SimpleRegistry

class WrapperRegistry<T>(registryKey: RegistryKey<Registry<T>>, lifecycle: Lifecycle, val wrapped: SimpleRegistry<T>, val default: T) : SimpleRegistry<T>(registryKey, lifecycle) {
    override fun get(id: Identifier?): T? {
        return wrapped.get(id) ?: default
    }
}
