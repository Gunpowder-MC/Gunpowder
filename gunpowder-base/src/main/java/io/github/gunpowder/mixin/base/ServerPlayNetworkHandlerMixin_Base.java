/*
 * MIT License
 *
 * Copyright (c) GunpowderMC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.gunpowder.mixin.base;

import io.github.gunpowder.api.GunpowderMod;
import io.github.gunpowder.entities.DimensionManager;
import io.github.gunpowder.mixin.cast.SyncPlayer;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;
import net.minecraft.world.biome.source.BiomeAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin_Base {
    @Shadow public abstract void sendPacket(Packet<?> packet);

    @Shadow public ServerPlayerEntity player;

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V", at = @At("HEAD"))
    void appendPackets(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> listener, CallbackInfo ci) {
        if (packet instanceof PlayerRespawnS2CPacket) {
            if (((SyncPlayer)player).needsSync()) {
                GunpowderMod.getInstance().getLogger().info("Player needs sync, sending packets");
                sendPacket(new GameJoinS2CPacket(
                        player.getEntityId(), player.interactionManager.getGameMode(), player.interactionManager.method_30119(),
                        BiomeAccess.hashSeed(player.getServerWorld().getSeed()), player.world.getLevelProperties().isHardcore(), DimensionManager.INSTANCE.getServer().getWorldRegistryKeys(),
                        DimensionManager.INSTANCE.getServer().registryManager, player.world.getDimension(), player.world.getRegistryKey(),
                        DimensionManager.INSTANCE.getServer().getPlayerManager().getMaxPlayerCount(), DimensionManager.INSTANCE.getServer().getPlayerManager().getViewDistance(),
                        player.world.getGameRules().getBoolean(GameRules.REDUCED_DEBUG_INFO),
                        !player.world.getGameRules().getBoolean(GameRules.DO_IMMEDIATE_RESPAWN),
                        player.world.isDebugWorld(), player.getServerWorld().isFlat()));
                sendPacket(new CloseScreenS2CPacket());
                ((SyncPlayer)player).setNeedsSync(false);
            }
        }
    }
}
