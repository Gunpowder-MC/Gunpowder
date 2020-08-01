package io.github.nyliummc.essentials.mixin.utilities;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin_Utilities {
    @Shadow @Final public PlayerEntity player;

    /**
     * @author Martmists
     * @reason lazy
     */
    @Overwrite
    public boolean canPlayerUse(PlayerEntity player) {
        return !this.player.removed;
    }
}
