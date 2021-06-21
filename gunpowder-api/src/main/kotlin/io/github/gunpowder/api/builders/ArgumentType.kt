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

package io.github.gunpowder.api.builders

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.suggestion.SuggestionProvider
import io.github.gunpowder.api.GunpowderMod
import net.minecraft.command.argument.serialize.ArgumentSerializer
import java.util.function.Consumer
import java.util.function.Function
import kotlin.reflect.KClass

/**
 * Kotlin builder for Colonel's ServerArgumentType
 */
interface ArgumentType {
    companion object{
        fun builder(block: Consumer<Builder>) = builder(block::accept)
        fun builder(block: (Builder) -> Unit) {
            val builder = GunpowderMod.instance.registry.getBuilder(Builder::class.java)
            block(builder)
            builder.register()
        }
    }

    interface Builder {
        /**
         * Argument identifier, for registration and via protocol.
         *
         * @param id The ID
         */
        fun id(id: String)

        /**
         * Set the native argument type.
         *
         * <p>A superclass is accepted within the type parameter to allow for parameterized argument types.
         * This does not allow for extensions types.</p>
         *
         * @param clazz Native argument type
         */
        fun type(clazz: Class<out ArgumentType<*>>)
        fun type(clazz: KClass<out ArgumentType<*>>) = type(clazz.java)


        /**
         * Set the provider to be sent to clients without this argument type.
         *
         * <p>The returned argument type may be provided as </p>
         *
         * @param fallback function taking own argument type and creating a
         */
        fun fallback(fallback: Function<ArgumentType<*>, ArgumentType<*>>) = fallback(fallback::apply)
        fun fallback(fallback: (ArgumentType<*>) -> ArgumentType<*>)

        /**
         * Set the serializer for the native argument type
         *
         * @param serializer serializer
         */
        fun serializer(serializer: ArgumentSerializer<ArgumentType<*>>)

        /**
         * Set the suggestion provider that will be set to clients that don't have this argument type.
         *
         * <p>By default, this is {@link SuggestionProviders#ASK_SERVER}, in order to use the full argument type's
         * suggestions. However, if the fallback type provides its own suggestions that meet requirements, this can be explicitly set to null</p>
         *
         * @param provider Provider for suggestions that will be sent to the client.
         */

        fun suggestions(provider: SuggestionProvider<*>)

        @Deprecated("Used internally, do not use.")
        fun register()
    }
}
