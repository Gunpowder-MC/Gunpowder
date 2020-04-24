package io.github.nyliummc.essentials

import io.github.nyliummc.essentials.api.EssentialsMod
import io.github.nyliummc.essentials.api.EssentialsModule
import io.github.nyliummc.essentials.api.modules.chat.modelhandlers.NicknameHandler as APINicknameHandler
import io.github.nyliummc.essentials.commands.NicknameCommand
import io.github.nyliummc.essentials.modelhandlers.NicknameHandler
import io.github.nyliummc.essentials.models.NicknameTable
import java.util.function.Supplier

class EssentialsChatModule : EssentialsModule {
    override val name = "chat"
    override val essentials: EssentialsMod
        get() = EssentialsMod.instance!!
    override val toggleable = true

    override fun onInitialize() {
        essentials.registry.registerTable(NicknameTable)

        essentials.registry.registerModelHandler(APINicknameHandler::class.java, Supplier { NicknameHandler } )

        essentials.registry.registerCommand(NicknameCommand::register)
    }

}
