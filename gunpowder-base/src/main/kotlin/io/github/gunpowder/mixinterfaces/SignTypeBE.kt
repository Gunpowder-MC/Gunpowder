package io.github.gunpowder.mixinterfaces

import io.github.gunpowder.api.types.SignType
import net.minecraft.entity.player.PlayerEntity

interface SignTypeBE {
    val isCustom: Boolean
    fun isCreator(player: PlayerEntity?): Boolean
    val signType: SignType?
}
