package io.github.gunpowder.api.components

import net.minecraft.nbt.CompoundTag

interface Component {
    /**
     * Serialization as supported by:
     * - Entity
     * - ServerPlayerEntity (with respawning)
     * - ItemStack
     * Alternatively, these can be stored in a database column as CompoundTag
     */
    fun toTag(tag: CompoundTag) {}
    fun fromTag(tag: CompoundTag) {}
}
