package io.github.gunpowder.entities

import io.github.gunpowder.api.components.getComponents
import io.github.gunpowder.api.components.with
import io.github.gunpowder.api.components.withAll
import net.minecraft.nbt.NbtCompound

object ComponentHandler {
    fun initComponents(it: Any) {
        for (component in it::class.java.getComponents()) {
            it.with(component)
        }
    }

    fun saveComponents(tag: NbtCompound, it: Any) {
        val ourTag = NbtCompound()
        for (component in it.withAll()) {
            component.writeNbt(ourTag)
        }
        tag.put("gprops", ourTag)
    }

    fun loadComponents(tag: NbtCompound, it: Any) {
        val ourTag = tag.getCompound("gprops")
        for (component in it.withAll()) {
            component.fromNbt(ourTag)
        }
    }
}
