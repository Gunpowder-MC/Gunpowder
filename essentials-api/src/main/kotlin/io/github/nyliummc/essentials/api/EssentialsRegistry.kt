package io.github.nyliummc.essentials.api

import io.github.nyliummc.essentials.api.extension.EssentialsExtension
import io.github.nyliummc.essentials.api.util.Builder
import java.util.function.Supplier

interface EssentialsRegistry {
    fun <T, B : Builder<T>> supplyBuilder(builderClass: Class<B>): B
    fun <T, B : Builder<T>> registerBuilder(builderClass: Class<B>, builderSupplier: Supplier<B>)
    fun <T : EssentialsExtension> registerExtension(extensionClass: Class<T>, instance: T)
}