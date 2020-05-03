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

import io.github.nyliummc.essentials.api.EssentialsMod
import io.github.nyliummc.essentials.configs.TeleportConfig
import net.minecraft.server.network.ServerPlayerEntity
import java.util.*

object TPACache {
    data class TPARequest(val teleportingEntity: ServerPlayerEntity, val targetLocationEntity: ServerPlayerEntity, val isTpaHere: Boolean) {
        fun byPlayer(p: ServerPlayerEntity): Boolean {
            return (p == requester())
        }

        fun toPlayer(p: ServerPlayerEntity): Boolean {
            return (p == requested())
        }

        fun requester(): ServerPlayerEntity {
            return if (isTpaHere) targetLocationEntity else teleportingEntity
        }

        fun requested(): ServerPlayerEntity {
            return if (!isTpaHere) targetLocationEntity else teleportingEntity
        }
    }

    val timeoutSeconds by lazy {
        EssentialsMod.instance.registry.getConfig(TeleportConfig::class.java).tpaTimeout
    }
    val cache = mutableListOf<TPARequest>()

    fun requestTpa(requester: ServerPlayerEntity, target: ServerPlayerEntity, callback: () -> Unit): Boolean {
        val req = TPARequest(requester, target, false)
        if (cache.any { it.byPlayer(requester) }) {
            // Only one TPA at a time max
            return false
        }
        cache.add(req)
        Timer().schedule(object : TimerTask() {
            override fun run() {
                if (cache.contains(req)) {
                    cache.remove(req)
                    callback()
                }
            }
        }, timeoutSeconds.toLong() * 1000)
        return true
    }

    fun requestTpaHere(requester: ServerPlayerEntity, target: ServerPlayerEntity, callback: () -> Unit): Boolean {
        val req = TPARequest(target, requester, true)
        if (cache.any { it.byPlayer(requester) }) {
            // Only one TPA at a time max
            return false
        }
        cache.add(req)
        Timer().schedule(object : TimerTask() {
            override fun run() {
                if (cache.contains(req)) {
                    cache.remove(req)
                    callback()
                }
            }
        }, timeoutSeconds.toLong() * 1000)
        return true
    }

    fun closeTpa(requester: ServerPlayerEntity?, requested: ServerPlayerEntity, callback: (TPARequest) -> Unit) {
        if (requester != null) {
            cache.forEach {
                if (it.byPlayer(requester) && it.toPlayer(requested)) {
                    callback(it)
                    cache.remove(it)
                }
                return
            }
        } else {
            cache.forEach {
                if (it.toPlayer(requested)) {
                    callback(it)
                    cache.remove(it)
                    return
                }
            }
        }
    }
}
