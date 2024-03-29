package io.github.gunpowder.mod.client

import com.google.common.collect.ImmutableSet
import io.github.gunpowder.mod.GunpowderRegistryImpl
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
            ServerPlayNetworking.registerGlobalReceiver(ID) { server: MinecraftServer, player: ServerPlayerEntity, _: ServerPlayNetworkHandler?, buffer: PacketByteBuf, _: PacketSender? ->
                val pkt = of(buffer)
                server.execute { // on main thread
                    GunpowderRegistryImpl.setKnownArgumentTypes(player, pkt.idents)
                }
            }
        }
    }
}
