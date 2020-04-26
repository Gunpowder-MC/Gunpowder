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

package io.github.nyliummc.essentials.entities

import com.mojang.brigadier.CommandDispatcher
import io.github.nyliummc.essentials.api.EssentialsMod
import io.github.nyliummc.essentials.commands.InfoCommand
import io.github.nyliummc.essentials.entities.builders.ChestGui
import io.github.nyliummc.essentials.entities.builders.Command
import io.github.nyliummc.essentials.entities.builders.TeleportRequest
import io.github.nyliummc.essentials.entities.builders.Text
import net.fabricmc.fabric.api.registry.CommandRegistry
import net.minecraft.server.command.ServerCommandSource
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.function.Supplier
import io.github.nyliummc.essentials.api.EssentialsRegistry as APIEssentialsRegistry
import io.github.nyliummc.essentials.api.builders.ChestGui as APIChestGui
import io.github.nyliummc.essentials.api.builders.Command as APICommand
import io.github.nyliummc.essentials.api.builders.TeleportRequest as APITeleportRequest
import io.github.nyliummc.essentials.api.builders.Text as APIText

object EssentialsRegistry : APIEssentialsRegistry {
    private val builders = mutableMapOf<Class<*>, Supplier<*>>()
    private val modelHandlers = mutableMapOf<Class<*>, Supplier<*>>()

    fun registerBuiltin() {
        registerCommand(InfoCommand::register)

        builders[APICommand.Builder::class.java] = Supplier { Command.Builder() }
        builders[APITeleportRequest.Builder::class.java] = Supplier { TeleportRequest.Builder() }
        builders[APIText.Builder::class.java] = Supplier { Text.Builder() }
        builders[APIChestGui.Builder::class.java] = Supplier { ChestGui.Builder() }
    }

    override fun registerCommand(callback: (CommandDispatcher<ServerCommandSource>) -> Unit) {
        CommandRegistry.INSTANCE.register(false, callback)
    }

    override fun registerTable(tab: Table) {
        transaction(EssentialsMod.instance!!.database.db) {
            SchemaUtils.createMissingTablesAndColumns(tab)
        }
    }

    override fun <T> getBuilder(clz: Class<T>): T {
        return builders[clz]!!.get() as T
    }

    override fun <T> getModelHandler(clz: Class<T>): T {
        return modelHandlers[clz]!!.get() as T
    }

    override fun <O : T, T> registerBuilder(clz: Class<T>, supplier: Supplier<O>) {
        builders[clz] = supplier
    }

    override fun <O : T, T> registerModelHandler(clz: Class<T>, supplier: Supplier<O>) {
        modelHandlers[clz] = supplier
    }
}
