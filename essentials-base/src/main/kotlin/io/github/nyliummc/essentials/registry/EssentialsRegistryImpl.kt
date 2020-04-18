package io.github.nyliummc.essentials.impl.registry

import io.github.nyliummc.essentials.api.EssentialsRegistry
import io.github.nyliummc.essentials.api.extension.EssentialsExtension
import io.github.nyliummc.essentials.api.util.Builder
import io.github.nyliummc.essentials.impl.EssentialsImpl
import java.util.*
import java.util.function.Supplier

class EssentialsRegistryImpl(private val essentials: EssentialsImpl) : EssentialsRegistry {
    private val builders: MutableMap<Class<out Builder<*>>, Supplier<out Builder<*>>> = IdentityHashMap()

    init {

    }

    override fun <T, B : Builder<T>> supplyBuilder(builderClass: Class<B>): B {
        @Suppress("UNCHECKED_CAST")
        return builders[builderClass]!!.get() as B
    }

    override fun <T, B : Builder<T>> registerBuilder(builderClass: Class<B>, builderSupplier: Supplier<B>) {
        builders[builderClass] = builderSupplier
    }

    override fun <T : EssentialsExtension> registerExtension(extensionClass: Class<T>, instance: T) {
        essentials.register(extensionClass, instance)
    }
}