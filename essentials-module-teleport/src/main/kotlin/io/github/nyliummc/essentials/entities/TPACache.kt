package io.github.nyliummc.essentials.entities

import net.minecraft.server.network.ServerPlayerEntity
import java.util.*

object TPACache {
    data class TPARequest(val teleportingEntity: ServerPlayerEntity, val targetLocationEntity: ServerPlayerEntity, val isTpaHere: Boolean) {
        fun byPlayer(q: ServerPlayerEntity): Boolean {
            return (q == requester())
        }

        fun toPlayer(q: ServerPlayerEntity): Boolean {
            return (q == requested())
        }

        fun requester(): ServerPlayerEntity {
            return if (isTpaHere) targetLocationEntity else teleportingEntity
        }

        fun requested(): ServerPlayerEntity {
            return if (!isTpaHere) targetLocationEntity else teleportingEntity
        }
    }

    val timeoutSeconds = 120
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
