package io.github.gunpowder.mixin.base;

import io.github.gunpowder.entities.ComponentHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(ItemStack.class)
public class ItemStackMixin_Base {
    @Inject(method="<init>(Lnet/minecraft/nbt/CompoundTag;)V", at=@At("RETURN"))
    void initComponents(CompoundTag tag, CallbackInfo ci) {
        ComponentHandler.INSTANCE.initComponents(this);
    }

    @Inject(method="<init>(Lnet/minecraft/item/ItemConvertible;ILjava/util/Optional;)V", at=@At("RETURN"))
    void initComponents(ItemConvertible itemConvertible, int count, Optional<CompoundTag> optional, CallbackInfo ci) {
        ComponentHandler.INSTANCE.initComponents(this);
    }

    @Inject(method="toTag", at=@At("HEAD"))
    void saveComponents(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        ComponentHandler.INSTANCE.saveComponents(tag, this);
    }

    @Inject(method="fromTag", at=@At("RETURN"))
    static void loadComponents(CompoundTag tag, CallbackInfoReturnable<ItemStack> cir) {
        ComponentHandler.INSTANCE.loadComponents(tag, cir.getReturnValue());
    }
}
