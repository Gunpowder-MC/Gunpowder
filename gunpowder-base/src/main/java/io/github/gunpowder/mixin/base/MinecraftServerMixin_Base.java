package io.github.gunpowder.mixin.base;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin_Base {
    @Inject(method="getServerModName", at=@At("HEAD"), cancellable = true, remap=false)
    void setCustomName(CallbackInfoReturnable<String> cir) {
        cir.setReturnValue("Fabric/Gunpowder");
    }
}
