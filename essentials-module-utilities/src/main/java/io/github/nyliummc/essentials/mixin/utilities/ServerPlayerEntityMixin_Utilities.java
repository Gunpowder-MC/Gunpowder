/*
 * MIT License
 *
 * Copyright (c) NyliumMC
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

package io.github.nyliummc.essentials.mixin.utilities;

import io.github.nyliummc.essentials.api.EssentialsMod;
import io.github.nyliummc.essentials.mixin.cast.PlayerVanish;
import net.fabricmc.fabric.impl.networking.server.EntityTrackerStreamAccessor;
import net.minecraft.network.MessageType;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin_Utilities implements PlayerVanish {
    private boolean vanished = false;

    @Override
    public boolean isVanished() {
        return vanished;
    }

    @Override
    public void setVanished(boolean enabled) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        vanished = enabled;

        player.setInvisible(enabled);

        // Sending player ADD/REMOVE packet
        // This updates the tablist
        final PlayerManager playerManager = EssentialsMod.getInstance().getServer().getPlayerManager();
        playerManager.sendToAll(
                new PlayerListS2CPacket(
                        enabled ? PlayerListS2CPacket.Action.REMOVE_PLAYER : PlayerListS2CPacket.Action.ADD_PLAYER,
                        player
                )
        );

        ThreadedAnvilChunkStorage storage = ((ServerChunkManager) player.world.getChunkManager()).threadedAnvilChunkStorage;
        EntityTrackerAccessor_Utilities trackerEntry = ((ThreadedAnvilChunkStorageAccessor_Utilities) storage).getEntityTrackers().get(player.getEntityId());

        // Starting / stopping the player tracking
        // This actually removes the player (otherwise hacked clients still see the it with certain hacks)
        ((EntityTrackerStreamAccessor) trackerEntry).fabric_getTrackingPlayers().forEach(
                tracking -> {
                    if (enabled)
                        trackerEntry.getEntry().stopTracking(tracking);
                    else
                        trackerEntry.getEntry().startTracking(tracking);

                }
        );

        // Faking leave - join message
        TranslatableText msg = new TranslatableText(
                enabled?
                        "multiplayer.player.left":
                        "multiplayer.player.joined",
                player.getDisplayName()
        );
        playerManager.broadcastChatMessage(msg.formatted(Formatting.YELLOW), MessageType.SYSTEM, Util.NIL_UUID);
    }
}
