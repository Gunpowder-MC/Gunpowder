/*
 * MIT License
 *
 * Copyright (c) GunpowderMC
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

package io.github.gunpowder.api

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.server.command.ServerCommandSource
import org.jetbrains.exposed.sql.Table
import java.util.function.Consumer
import java.util.function.Supplier

interface GunpowderRegistry {
    /**
     * Register a command; This should be a consumer taking a CommandDispatcher<ServerCommandSource>
     */
    fun registerCommand(callback: Consumer<CommandDispatcher<ServerCommandSource>>) = registerCommand(callback::accept)
    fun registerCommand(callback: (CommandDispatcher<ServerCommandSource>) -> Unit)

    /**
     * Register an Exposed table object. Creating the columns in the database is handled internally.
     */
    fun registerTable(tab: Table)

    /**
     * Get the working config for the registered config class.
     */
    fun <T> getConfig(clz: Class<T>): T

    /**
     * Get the Builder implementation for an API builder.
     */
    fun <T> getBuilder(clz: Class<T>): T

    /**
     * Get the ModelHandler implementation for an API modelhandler.
     */
    fun <T> getModelHandler(clz: Class<T>): T

    /**
     * Register a Builder to provide an API builder.
     */
    fun <O : T, T> registerBuilder(clz: Class<T>, supplier: Supplier<O>)

    /**
     * Register a model handler to provide an API model handler.
     */
    fun <O : T, T> registerModelHandler(clz: Class<T>, supplier: Supplier<O>)

    /**
     * Register a config with a default, being either the path of a resource or a config object.
     */
    fun registerConfig(filename: String, cfg: Class<*>, default: Any)
}
