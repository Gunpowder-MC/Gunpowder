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

package io.github.gunpowder.entities.mc

import com.google.common.collect.ImmutableSet
import io.github.gunpowder.entities.arguments.ServerArgumentTypes
import io.netty.buffer.Unpooled
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier


class RegisteredArgumentTypesC2SPacket(val idents: Set<Identifier>) {
    fun toPacket(buffer: PacketByteBuf) {
        buffer.writeVarInt(idents.size)
        for (id in idents) {
            buffer.writeIdentifier(id)
        }
    }

    fun sendTo(sender: PacketSender) {
        if (ClientPlayNetworking.canSend(ID)) {
            val buffer = PacketByteBuf(Unpooled.buffer(idents.size * 8))
            toPacket(buffer)
            sender.sendPacket(ID, buffer)
        }
    }

    companion object {
        // Backwards compat with existing colonel clients
        val ID = Identifier("colonel:registered-args")

        fun of(buf: PacketByteBuf): RegisteredArgumentTypesC2SPacket {
            val length = buf.readVarInt()
            val items = ImmutableSet.builder<Identifier>()
            for (i in 0 until length) {
                items.add(buf.readIdentifier())
            }
            return RegisteredArgumentTypesC2SPacket(items.build())
        }

        fun register() {
            ServerPlayNetworking.registerGlobalReceiver(ID) { server: MinecraftServer, player: ServerPlayerEntity, handler: ServerPlayNetworkHandler?, buffer: PacketByteBuf, responder: PacketSender? ->
                val pkt = of(buffer)
                server.execute { // on main thread
                    ServerArgumentTypes.setKnownArgumentTypes(player, pkt.idents)
                }
            }
        }
    }
}
