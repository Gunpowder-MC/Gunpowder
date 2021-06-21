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

package io.github.gunpowder.entities.builders

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.suggestion.SuggestionProvider
import io.github.gunpowder.entities.arguments.ServerArgumentTypes
import net.minecraft.command.argument.ArgumentTypes
import net.minecraft.command.argument.serialize.ArgumentSerializer
import net.minecraft.command.suggestion.SuggestionProviders
import net.minecraft.util.Identifier
import io.github.gunpowder.api.builders.ArgumentType as APIArgumentType


class ArgumentType(
    val id: Identifier,
    val type: Class<out ArgumentType<*>>,
    val fallback: (ArgumentType<*>) -> ArgumentType<*>,
    val serializer: ArgumentSerializer<ArgumentType<*>>,
    val suggestions: SuggestionProvider<*>?
) : APIArgumentType {


    class Builder : APIArgumentType.Builder {
        private lateinit var serializer: ArgumentSerializer<ArgumentType<*>>
        private lateinit var id: String
        private lateinit var type: Class<out ArgumentType<*>>
        private lateinit var fallback: (ArgumentType<*>) -> ArgumentType<*>
        private var suggestions: SuggestionProvider<*> = SuggestionProviders.ASK_SERVER

        override fun id(id: String) {
            this.id = id
        }

        override fun type(clazz: Class<out ArgumentType<*>>) {
            this.type = clazz
        }

        override fun fallback(fallback: (ArgumentType<*>) -> ArgumentType<*>) {
            this.fallback = fallback
        }

        override fun serializer(serializer: ArgumentSerializer<ArgumentType<*>>) {
            this.serializer = serializer
        }

        override fun suggestions(provider: SuggestionProvider<*>) {
            this.suggestions = provider
        }

        override fun register() {
            ArgumentTypes.register(id, type as Class<ArgumentType<*>>, serializer)
            val argumentType = ArgumentType(Identifier(id), type, fallback, serializer, suggestions)
            ServerArgumentTypes.register(argumentType)
        }
    }
}
