package io.github.gunpowder.mixin.base;

import io.github.gunpowder.entities.DimensionManager;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractFireBlock.class)
public class AbstractFireBlockMixin_Base {
    @Inject(method="method_30366", at=@At("HEAD"), cancellable = true)
    private static void supportDimensions(World world, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(
                DimensionManager.INSTANCE.getNetherMap().containsKey(world.getRegistryKey()) ||
                DimensionManager.INSTANCE.getNetherMap().containsValue(world.getRegistryKey())
        );
    }
}
