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

package io.github.gunpowder.entities.builtin

import io.github.gunpowder.api.components.Component
import io.github.gunpowder.api.components.with
import io.github.gunpowder.entities.builders.SignType
import net.minecraft.block.entity.SignBlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.world.World
import java.util.*

class SignTypeComponent : Component<SignBlockEntity>() {
    var custom = false
    lateinit var type: SignType
    lateinit var owner: UUID

    override fun fromNbt(tag: NbtCompound) {
        if (!tag.contains("gp:sign")) {
            return
        }
        val tag = tag.getCompound("gp:sign")
        val _type = SignType.registry[Identifier(tag.getString("gunpowder:customType"))] as SignType?
        if (_type != null) {
            type = _type
            type.deserializeEvent.invoke(bound, tag)
            custom = true
            owner = tag.getUuid("gunpowder:owner")
        }
    }

    override fun writeNbt(tag: NbtCompound) {
        if (custom) {
            val newTag = NbtCompound()
            newTag.putString("gunpowder:customType", SignType.registry.getId(type).toString())
            newTag.putUuid("gunpowder:owner", owner)
            type.serializeEvent.invoke(bound, newTag)
            tag.put("gp:sign", newTag)
        }
    }

    fun onActivate(player: ServerPlayerEntity) : Boolean {
        if (!player.world.isClient() && custom) {
            type.clickEvent.invoke(bound, player)
            return true;
        }
        return false;
    }

    fun create(owner: UUID, type: SignType) {
        this.owner = owner
        this.type = type
        custom = true
    }

    fun isCreator(player: ServerPlayerEntity): Boolean {
        return player.uuid.equals(owner)
    }
}
