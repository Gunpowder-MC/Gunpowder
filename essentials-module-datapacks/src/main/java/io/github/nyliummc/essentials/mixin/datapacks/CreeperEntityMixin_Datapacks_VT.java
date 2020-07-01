package io.github.nyliummc.essentials.mixin.datapacks;

import io.github.nyliummc.essentials.api.EssentialsMod;
import io.github.nyliummc.essentials.configs.DatapacksConfig;
import io.github.nyliummc.essentials.configs.VanillaTweaksConfig;
import net.minecraft.entity.mob.CreeperEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreeperEntity.class)
public class CreeperEntityMixin_Datapacks_VT {
    @Shadow private int explosionRadius;

    @Inject(method="explode", at=@At("HEAD"))
    void setExplosionStrength(CallbackInfo ci) {
        explosionRadius = EssentialsMod.getInstance().getRegistry().getConfig(DatapacksConfig.class).getVanillaTweaks().getCreeperExplosionStrength();
    }
}
