package io.github.nyliummc.essentials.mixin.dynmap;

import io.github.nyliummc.essentials.EssentialsDynmapModule;
import io.github.nyliummc.essentials.entities.FabricDynmapOnlinePlayer;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.dynmap.common.DynmapListenerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin_Dynmap {
    @Inject(method = "<init>", at=@At("RETURN"))
    void event(MinecraftServer minecraftServer, ClientConnection clientConnection, ServerPlayerEntity serverPlayerEntity, CallbackInfo ci) {
        EssentialsDynmapModule.core.listenerManager.processPlayerEvent(DynmapListenerManager.EventType.PLAYER_JOIN, new FabricDynmapOnlinePlayer(serverPlayerEntity));
    }
}
