package io.github.gunpowder.mixin.base;

import io.github.gunpowder.entities.ComponentHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemConvertible;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntity.class)
public class BlockEntityMixin_Base {
    @Inject(method="<init>", at=@At("RETURN"))
    void initComponents(BlockEntityType<?> type, CallbackInfo ci) {
        ComponentHandler.INSTANCE.initComponents(this);
    }

    @Inject(method="toTag", at=@At("HEAD"))
    void saveComponents(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        ComponentHandler.INSTANCE.saveComponents(tag, this);
    }

    @Inject(method="fromTag", at=@At("HEAD"))
    void loadComponents(BlockState state, CompoundTag tag, CallbackInfo ci) {
        ComponentHandler.INSTANCE.loadComponents(tag, this);
    }
}
