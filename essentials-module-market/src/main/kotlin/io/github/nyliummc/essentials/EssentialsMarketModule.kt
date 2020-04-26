/*
 * MIT License
 *
 * Copyright (c) NyliumMC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.nyliummc.essentials

import io.github.nyliummc.essentials.api.EssentialsMod
import io.github.nyliummc.essentials.api.EssentialsModule
import io.github.nyliummc.essentials.commands.MarketCommand
import io.github.nyliummc.essentials.modelhandlers.MarketEntryHandler
import io.github.nyliummc.essentials.models.MarketEntryTable
import java.util.function.Supplier
import io.github.nyliummc.essentials.api.modules.market.modelhandlers.MarketEntryHandler as APIMarketEntryHandler

class EssentialsMarketModule : EssentialsModule {
    override val name = "market"
    override val essentials by lazy {
        EssentialsMod.instance!!
    }
    override val toggleable = true

    override fun registerCommands() {
        essentials.registry.registerCommand(MarketCommand::register)
    }

    override fun onInitialize() {
        essentials.registry.registerTable(MarketEntryTable)
        essentials.registry.registerModelHandler(APIMarketEntryHandler::class.java, Supplier { MarketEntryHandler } )
    }

}
