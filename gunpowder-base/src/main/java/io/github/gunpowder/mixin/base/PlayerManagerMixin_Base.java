package io.github.gunpowder.mixin.base;

import io.github.gunpowder.mixin.MixinHooks;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin_Base {
    @Inject(method="onPlayerConnect", at=@At("HEAD"))
    void registerPlayer(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        MixinHooks.createPlayerEntry(player);
    }
}
