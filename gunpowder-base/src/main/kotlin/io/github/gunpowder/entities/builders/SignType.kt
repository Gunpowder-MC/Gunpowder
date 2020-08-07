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

import com.mojang.serialization.Lifecycle
import io.github.gunpowder.api.GunpowderMod
import io.github.gunpowder.api.GunpowderMod.Companion.instance
import net.minecraft.block.entity.SignBlockEntity
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryKey
import net.minecraft.util.registry.SimpleRegistry
import io.github.gunpowder.api.builders.SignType as APISignType

class SignType(val clickEvent: (SignBlockEntity, ServerPlayerEntity) -> Unit,
               val createEvent: (SignBlockEntity, ServerPlayerEntity) -> Unit,
               val destroyEvent: (SignBlockEntity, ServerPlayerEntity) -> Unit,
               val serializeEvent: (SignBlockEntity, CompoundTag) -> Unit,
               val deserializeEvent: (SignBlockEntity, CompoundTag) -> Unit,
               val conditionEvent: (SignBlockEntity, ServerPlayerEntity) -> Boolean) : APISignType {

    companion object {
        private val key = RegistryKey.ofRegistry<APISignType>(Identifier("gunpowder:sign_type"))
        val registry = SimpleRegistry<APISignType>(key, Lifecycle.stable())
    }

    class Builder : APISignType.Builder {
        var id = Identifier("unused_sign_type_too_long_for_sign")
        var clickEvent: (SignBlockEntity, ServerPlayerEntity) -> Unit = { _, _ -> }
        var requireEvent: (SignBlockEntity, ServerPlayerEntity) -> Boolean = { _, _ -> true }
        var createEvent: (SignBlockEntity, ServerPlayerEntity) -> Unit = { _, _ -> }
        var destroyEvent: (SignBlockEntity, ServerPlayerEntity) -> Unit = { _, _ -> }
        var serializeEvent: (SignBlockEntity, CompoundTag) -> Unit = { _, _ -> }
        var deserializeEvent: (SignBlockEntity, CompoundTag) -> Unit = { _, _ -> }

        override fun name(name: Identifier) {
            id = name
        }

        override fun requires(condition: (SignBlockEntity, ServerPlayerEntity) -> Boolean) {
            requireEvent = condition
        }

        override fun onClicked(callback: (SignBlockEntity, ServerPlayerEntity) -> Unit) {
            clickEvent = callback
        }

        override fun onCreated(callback: (SignBlockEntity, ServerPlayerEntity) -> Unit) {
            createEvent = callback
        }

        override fun onDestroyed(callback: (SignBlockEntity, ServerPlayerEntity) -> Unit) {
            destroyEvent = callback
        }

        override fun serialize(callback: (SignBlockEntity, CompoundTag) -> Unit) {
            serializeEvent = callback
        }

        override fun deserialize(callback: (SignBlockEntity, CompoundTag) -> Unit) {
            deserializeEvent = callback
        }

        override fun build() {
            Registry.register(registry, id, SignType(clickEvent, createEvent, destroyEvent, serializeEvent, deserializeEvent, requireEvent))
        }
    }
}
