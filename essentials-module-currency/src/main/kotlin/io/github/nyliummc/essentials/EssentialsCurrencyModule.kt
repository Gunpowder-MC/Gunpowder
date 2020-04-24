package io.github.nyliummc.essentials

import io.github.nyliummc.essentials.api.EssentialsMod
import io.github.nyliummc.essentials.api.EssentialsModule
import io.github.nyliummc.essentials.modelhandlers.BalanceHandler
import io.github.nyliummc.essentials.models.BalanceTable
import java.util.function.Supplier
import io.github.nyliummc.essentials.api.modules.currency.modelhandlers.BalanceHandler as APIBalanceHandler

class EssentialsCurrencyModule : EssentialsModule {
    override val name = "currency"
    override val essentials: EssentialsMod
        get() = EssentialsMod.instance!!
    override val toggleable = true

    override fun onInitialize() {
        essentials.registry.registerTable(BalanceTable)

        essentials.registry.registerModelHandler(APIBalanceHandler::class.java, Supplier { BalanceHandler })
    }

}
