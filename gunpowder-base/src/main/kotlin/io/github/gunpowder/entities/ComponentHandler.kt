package io.github.gunpowder.entities

import io.github.gunpowder.api.components.getComponents
import io.github.gunpowder.api.components.with
import io.github.gunpowder.api.components.withAll
import net.minecraft.nbt.CompoundTag

object ComponentHandler {
    fun initComponents(it: Any) {
        for (component in it::class.java.getComponents()) {
            it.with(component)
        }
    }

    fun saveComponents(tag: CompoundTag, it: Any) {
        val ourTag = CompoundTag()
        for (component in it.withAll()) {
            component.toTag(ourTag)
        }
        tag.put("gprops", ourTag)
    }

    fun loadComponents(tag: CompoundTag, it: Any) {
        val ourTag = tag.getCompound("gprops")
        for (component in it.withAll()) {
            component.fromTag(ourTag)
        }
    }
}
