package io.github.nyliummc.essentials.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
abstract class PlayerEntityMixin extends Entity {
    public PlayerEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
    void getDisplayName(CallbackInfoReturnable<Text> cir) {
        if (this.isCustomNameVisible() && this.hasCustomName()) {
            cir.setReturnValue(this.getCustomName());
        }
    }
}