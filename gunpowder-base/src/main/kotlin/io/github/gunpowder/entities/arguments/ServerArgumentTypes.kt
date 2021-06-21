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

package io.github.gunpowder.entities.arguments

import com.mojang.brigadier.arguments.ArgumentType
import io.github.gunpowder.api.components.with
import io.github.gunpowder.entities.builtin.PlayerArgumentComponent
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import io.github.gunpowder.entities.builders.ArgumentType as ImplArgumentType


object ServerArgumentTypes {
    private val BY_TYPE: MutableMap<Class<*>, ImplArgumentType> = HashMap()
    private val BY_ID: MutableMap<Identifier, ImplArgumentType> =
        ConcurrentHashMap<Identifier, ImplArgumentType>()

    @JvmStatic
    fun <T : ArgumentType<*>?> byClass(clazz: Class<T>): ImplArgumentType? {
        return BY_TYPE[clazz]
    }

    @JvmStatic
    fun register(type: ImplArgumentType) {
        BY_TYPE[type.type] = type
        BY_ID[type.id] = type
    }

    @JvmStatic
    val ids: Set<Identifier>
        get() = Collections.unmodifiableSet(BY_ID.keys)

    @JvmStatic
    fun setKnownArgumentTypes(player: PlayerEntity, ids: Set<Identifier>) {
        if (player is ServerPlayerEntity) {
            val known = player.with<PlayerArgumentComponent>().known
            known.clear()
            if (ids.isNotEmpty()) {
                known.addAll(ids)
                // TODO: Avoid resending the whole command tree, find a way to receive the packet before sending?
                player.server.playerManager.sendCommandTree(player)
            }
        }
    }

    @JvmStatic
    fun getKnownArgumentTypes(player: ServerPlayerEntity): Set<Identifier> {
        return player.with<PlayerArgumentComponent>().known
    }
}
