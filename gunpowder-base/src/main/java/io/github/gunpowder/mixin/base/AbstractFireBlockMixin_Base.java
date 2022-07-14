package io.github.gunpowder.mixin.base;

import com.google.common.collect.BiMap;
import io.github.gunpowder.mod.GunpowderRegistryImpl;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractFireBlock.class)
public abstract class AbstractFireBlockMixin_Base {
    @Inject(method="isOverworldOrNether", at=@At("HEAD"), cancellable = true)
    private static void supportDimensions(World world, CallbackInfoReturnable<Boolean> cir) {
        BiMap<RegistryKey<World>, RegistryKey<World>> map = GunpowderRegistryImpl.INSTANCE.getNetherMap();
        cir.setReturnValue(
                map.containsKey(world.getRegistryKey()) || map.containsValue(world.getRegistryKey())
        );
    }
}
