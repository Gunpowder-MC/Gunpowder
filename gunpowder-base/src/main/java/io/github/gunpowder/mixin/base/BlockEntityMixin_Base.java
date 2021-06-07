package io.github.gunpowder.mixin.base;

import io.github.gunpowder.entities.ComponentHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntity.class)
public class BlockEntityMixin_Base {
    @Inject(method="<init>", at=@At("RETURN"))
    void initComponents(BlockEntityType<?> type, BlockPos pos, BlockState state, CallbackInfo ci) {
        ComponentHandler.INSTANCE.initComponents(this);
    }

    @Inject(method="writeNbt", at=@At("HEAD"))
    void saveComponents(NbtCompound tag, CallbackInfoReturnable<NbtCompound> cir) {
        ComponentHandler.INSTANCE.saveComponents(tag, this);
    }

    @Inject(method="readNbt", at=@At("HEAD"))
    void loadComponents(NbtCompound tag, CallbackInfo ci) {
        ComponentHandler.INSTANCE.loadComponents(tag, this);
    }
}
