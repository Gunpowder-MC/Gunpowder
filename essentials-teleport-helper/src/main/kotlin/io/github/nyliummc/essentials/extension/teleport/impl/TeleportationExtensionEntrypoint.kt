package io.github.nyliummc.essentials.extension.teleport.impl

import io.github.nyliummc.essentials.api.Essentials
import io.github.nyliummc.essentials.api.EssentialsRegistry
import io.github.nyliummc.essentials.api.extension.EssentialsExtension
import io.github.nyliummc.essentials.api.extension.ExtensionEntrypoint
import io.github.nyliummc.essentials.impl.EssentialsImpl
import io.github.nyliummc.essentials.extension.teleport.api.TeleportExtension
import io.github.nyliummc.essentials.extension.teleport.api.Teleportation
import java.util.function.Supplier

class TeleportationExtensionEntrypoint: ExtensionEntrypoint {
    override fun <T : EssentialsExtension> registerExtensions(essentials: Essentials, registry: EssentialsRegistry) {
        registry.registerExtension(TeleportExtension::class.java, TeleportExtensionImpl(essentials as EssentialsImpl))
    }

    override fun registerBuilders(registry: EssentialsRegistry) {
        registry.registerBuilder(Teleportation.Builder::class.java, Supplier<Teleportation.Builder> { TeleportationBuilderImpl() })
    }
}