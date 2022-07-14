package io.github.gunpowder.api.types

import net.minecraft.block.entity.SignBlockEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier

interface SignType {
    val id: Identifier
    fun canCreate(sign: SignBlockEntity, player: ServerPlayerEntity): Boolean
    fun onCreate(sign: SignBlockEntity, player: ServerPlayerEntity)
    fun onDestroy(sign: SignBlockEntity, player: ServerPlayerEntity)
    fun onClick(sign: SignBlockEntity, player: ServerPlayerEntity)
    fun serialize(sign: SignBlockEntity, tag: NbtCompound)
    fun deserialize(sign: SignBlockEntity, tag: NbtCompound)
}
