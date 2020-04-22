package io.github.nyliummc.essentials.extensions

import io.github.nyliummc.essentials.api.Essentials
import io.github.nyliummc.essentials.base.extension.AbstractEssentialsExtension
import io.github.nyliummc.essentials.commands.CommandHome

class TPExtension(essentials: Essentials) : AbstractEssentialsExtension("teleport", essentials) {
    init {
        essentials.registry.registerCommand(CommandHome::register)
    }
}