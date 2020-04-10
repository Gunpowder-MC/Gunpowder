package io.github.nyliummc.essentials.api.extension

import io.github.nyliummc.essentials.api.Essentials
import io.github.nyliummc.essentials.api.EssentialsRegistry

interface ExtensionEntrypoint {
    fun <T : EssentialsExtension> registerExtensions(essentials: Essentials, registry: EssentialsRegistry)
    fun registerBuilders(registry: EssentialsRegistry)
}