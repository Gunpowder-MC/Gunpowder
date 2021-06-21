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

package io.github.gunpowder.api.components

import net.minecraft.nbt.NbtCompound

abstract class Component<T : Any> {
    lateinit var bound: T
    internal fun setBound(it: Any) {
        bound = it as T
    }

    /**
     * Ticking supported by:
     * - Entity
     * - Chunk
     * - ServerWorld
     * - BlockEntity
     */
    open fun tick() {}

    /**
     * Serialization as supported by:
     * - Entity
     * - ServerPlayerEntity (with respawning)
     * - ItemStack
     * - ServerWorld (experimental)
     * - BlockEntity
     * Alternatively, these can be stored in a database column as NbtCompound
     */
    open fun writeNbt(tag: NbtCompound) {}

    open fun fromNbt(tag: NbtCompound) {}
}