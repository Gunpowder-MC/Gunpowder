package io.github.gunpowder.api.components

import net.minecraft.nbt.NbtCompound

interface Component {
    /**
     * Serialization as supported by:
     * - Entity
     * - ServerPlayerEntity (with respawning)
     * - ItemStack
     * Alternatively, these can be stored in a database column as NbtCompound
     */
    fun writeNbt(tag: NbtCompound) {}
    fun fromNbt(tag: NbtCompound) {}
}
