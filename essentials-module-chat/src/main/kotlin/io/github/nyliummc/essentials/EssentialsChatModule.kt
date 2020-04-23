package io.github.nyliummc.essentials

import io.github.nyliummc.essentials.api.EssentialsMod
import io.github.nyliummc.essentials.api.EssentialsModule
import io.github.nyliummc.essentials.commands.NicknameCommand

class EssentialsChatModule : EssentialsModule {
    override val name = ""
    override val essentials: EssentialsMod
        get() = EssentialsMod.instance!!
    override val toggleable = true

    override fun onInitialize() {
        essentials.registry.registerCommand(NicknameCommand::register)
    }

}
