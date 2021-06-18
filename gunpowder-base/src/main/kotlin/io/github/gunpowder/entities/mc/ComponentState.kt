package io.github.gunpowder.entities.mc

import io.github.gunpowder.api.components.withAll
import io.github.gunpowder.entities.ComponentHandler
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.PersistentState

class ComponentState(val bound: ServerWorld) : PersistentState() {
    override fun writeNbt(nbt: NbtCompound): NbtCompound {
        ComponentHandler.saveComponents(nbt, bound)
        return nbt
    }

    companion object {
        @JvmStatic
        fun fromNbt(nbt: NbtCompound, it: ServerWorld) : ComponentState {
            return ComponentState(it).apply {
                ComponentHandler.loadComponents(nbt, bound)
            }
        }
    }

}
