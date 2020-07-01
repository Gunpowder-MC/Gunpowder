package io.github.nyliummc.essentials.mixin.datapacks;

import io.github.nyliummc.essentials.api.EssentialsMod;
import io.github.nyliummc.essentials.configs.DatapacksConfig;
import io.github.nyliummc.essentials.configs.VanillaTweaksConfig;
import net.minecraft.entity.mob.GhastEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GhastEntity.class)
public class GhastEntityMixin_Datapacks_VT {
    @Inject(method="getFireballStrength", at=@At("RETURN"), cancellable=true)
    void setExplosionStrength(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(EssentialsMod.getInstance().getRegistry().getConfig(DatapacksConfig.class).getVanillaTweaks().getGhastExplosionStrength());
    }
}
