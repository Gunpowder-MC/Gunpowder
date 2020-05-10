package io.github.nyliummc.essentials.mixin.dynmap;

import io.github.nyliummc.essentials.EssentialsDynmapModule;
import io.github.nyliummc.essentials.entities.FabricDynmapOnlinePlayer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.dynmap.common.DynmapListenerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin_Dynmap {
    @Inject(method="remove", at=@At("HEAD"))
    void event(ServerPlayerEntity player, CallbackInfo ci) {
        EssentialsDynmapModule.core.listenerManager.processPlayerEvent(DynmapListenerManager.EventType.PLAYER_QUIT, new FabricDynmapOnlinePlayer(player));
    }
}
