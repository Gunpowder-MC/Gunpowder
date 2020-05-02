package io.github.nyliummc.essentials.mixin.utilities;

import io.github.nyliummc.essentials.EssentialsUtilitiesModule;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Dimension.class)
public class DimensionMixin_Utilities {
    @Shadow
    @Final
    private DimensionType type;

    // Called every tick, injecting here instead of World to get access to `type`
    @Inject(method = "hasSkyLight", at = @At("RETURN"))
    public void tick(CallbackInfoReturnable<Boolean> cir) {
        EssentialsUtilitiesModule.getTracker(type.toString()).tick();
    }
}
