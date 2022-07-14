package io.github.gunpowder.mixin.base;

import io.github.gunpowder.mixinterfaces.SyncPlayer;
import io.github.gunpowder.mod.GunpowderRegistryImpl;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.GameRules;
import net.minecraft.world.biome.source.BiomeAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin_Base {
    @Shadow
    public ServerPlayerEntity player;

    @Shadow
    public abstract void sendPacket(Packet<?> packet);

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V", at = @At("HEAD"))
    void appendPackets(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> listener, CallbackInfo ci) {
        if (packet instanceof PlayerRespawnS2CPacket && ((SyncPlayer) player).needsSync()) {
            sendPacket(new GameJoinS2CPacket(
                    player.getId(),
                    player.world.getLevelProperties().isHardcore(),
                    player.interactionManager.getGameMode(),
                    player.interactionManager.getPreviousGameMode(),
                    GunpowderRegistryImpl.INSTANCE.getServer().getWorldRegistryKeys(),
                    GunpowderRegistryImpl.INSTANCE.getServer().registryManager,
                    new RegistryEntry.Direct<>(player.world.getDimension()),
                    player.world.getRegistryKey(),
                    BiomeAccess.hashSeed(player.getWorld().getSeed()),
                    GunpowderRegistryImpl.INSTANCE.getServer().getPlayerManager().getMaxPlayerCount(),
                    GunpowderRegistryImpl.INSTANCE.getServer().getPlayerManager().getViewDistance(),
                    GunpowderRegistryImpl.INSTANCE.getServer().getPlayerManager().getSimulationDistance(),
                    player.world.getGameRules().getBoolean(GameRules.REDUCED_DEBUG_INFO),
                    !player.world.getGameRules().getBoolean(GameRules.DO_IMMEDIATE_RESPAWN),
                    player.world.isDebugWorld(),
                    player.getWorld().isFlat()));
            sendPacket(new CloseScreenS2CPacket(0));
            ((SyncPlayer) player).setNeedsSync(false);
        }
    }
}
