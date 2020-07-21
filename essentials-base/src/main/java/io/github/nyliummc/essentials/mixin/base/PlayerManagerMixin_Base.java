package io.github.nyliummc.essentials.mixin.base;


import io.github.nyliummc.essentials.entities.KotlinEventHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin_Base {
    @Inject(method = "onPlayerConnect", at = @At("HEAD"))
    void registerToTable(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        KotlinEventHandler.playerConnect(player);
    }
}
