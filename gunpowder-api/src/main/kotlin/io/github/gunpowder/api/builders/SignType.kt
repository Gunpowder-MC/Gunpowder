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

import io.github.gunpowder.api.GunpowderMod
import net.minecraft.block.entity.SignBlockEntity
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import java.util.function.BiConsumer
import java.util.function.Consumer

interface SignType {
    companion object {
        @JvmStatic
        fun builder(callback: Consumer<Builder>) = builder(callback::accept)
        fun builder(callback: Builder.() -> Unit) {
            val builder = GunpowderMod.instance.registry.getBuilder(Builder::class.java)
            callback(builder)
            return builder.build()
        }
    }

    interface Builder {
        fun name(name: String) = name(Identifier(name))
        fun name(name: Identifier)

        fun onClicked(callback: BiConsumer<SignBlockEntity, ServerPlayerEntity>) = onClicked(callback::accept)
        fun onClicked(callback: (SignBlockEntity, ServerPlayerEntity) -> Unit)

        fun onCreated(callback: BiConsumer<SignBlockEntity, ServerPlayerEntity>) = onCreated(callback::accept)
        fun onCreated(callback: (SignBlockEntity, ServerPlayerEntity) -> Unit)

        fun onDestroyed(callback: BiConsumer<SignBlockEntity, ServerPlayerEntity>) = onDestroyed(callback::accept)
        fun onDestroyed(callback: (SignBlockEntity, ServerPlayerEntity) -> Unit)

        fun serialize(callback: BiConsumer<SignBlockEntity, CompoundTag>) = serialize(callback::accept)
        fun serialize(callback: (SignBlockEntity, CompoundTag) -> Unit)

        fun deserialize(callback: BiConsumer<SignBlockEntity, CompoundTag>) = deserialize(callback::accept)
        fun deserialize(callback: (SignBlockEntity, CompoundTag) -> Unit)

        @Deprecated("Used internally, do not use.")
        fun build()
    }
}
