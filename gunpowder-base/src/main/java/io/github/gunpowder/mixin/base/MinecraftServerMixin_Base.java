package io.github.gunpowder.mixin.base;

import io.github.gunpowder.api.GunpowderMod;
import io.github.gunpowder.mod.AbstractGunpowderMod;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin_Base {
    @Inject(method="reloadResources", at=@At("RETURN"))
    void reloadModules(Collection<String> collection, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        ((AbstractGunpowderMod) GunpowderMod.getInstance()).reload$gunpowder_base();
    }
}
