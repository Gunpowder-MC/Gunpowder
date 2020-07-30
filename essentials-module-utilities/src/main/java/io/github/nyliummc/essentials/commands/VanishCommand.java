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

package io.github.nyliummc.essentials.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.nyliummc.essentials.mixin.cast.PlayerVanish;
import io.github.nyliummc.essentials.mixin.utilities.EntityTrackerAccessor_Utilities;
import io.github.nyliummc.essentials.mixin.utilities.ThreadedAnvilChunkStorageAccessor_Utilities;
import net.fabricmc.fabric.impl.networking.server.EntityTrackerStreamAccessor;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.text.LiteralText;

import java.util.Objects;

import static net.minecraft.server.command.CommandManager.literal;

public class VanishCommand {


    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("vanish")
            .requires(source -> source.hasPermissionLevel(4))
            .then(literal("toggle")
                    .executes(ctx -> toggleVanish(ctx.getSource()))
            )
            .executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayer();
                    player.sendMessage(
                            new LiteralText(
                                    ((PlayerVanish) player).isVanished() ?
                                        "§6You are vanished." :
                                        "§6You're not vanished."
                            ),
                            true
                    );
                    return 1;
                }
            )
        );
    }

    private static int toggleVanish(ServerCommandSource src) throws CommandSyntaxException {
        ServerPlayerEntity player = src.getPlayer();

        // Storing vanished status in boolean to ease the use
        final boolean isVanished = ((PlayerVanish) player).isVanished();

        player.setInvisible(!isVanished);
        ((PlayerVanish) player).setVanished(!isVanished);

        // Sending player ADD/REMOVE packet
        // This updates the tablist
        Objects.requireNonNull(player.getServer()).getPlayerManager().sendToAll(
                new PlayerListS2CPacket(
                        isVanished ? PlayerListS2CPacket.Action.ADD_PLAYER : PlayerListS2CPacket.Action.REMOVE_PLAYER,
                        player
                )
        );

        ThreadedAnvilChunkStorage storage = ((ServerChunkManager) player.world.getChunkManager()).threadedAnvilChunkStorage;
        EntityTrackerAccessor_Utilities trackerEntry = ((ThreadedAnvilChunkStorageAccessor_Utilities) storage).getEntityTrackers().get(player.getEntityId());

        // Starting / stopping the player tracking
        ((EntityTrackerStreamAccessor) trackerEntry).fabric_getTrackingPlayers().forEach(
                tracking -> {
                    if (isVanished)
                        trackerEntry.getEntry().startTracking(tracking);
                    else
                        trackerEntry.getEntry().stopTracking(tracking);
                }
        );

        // Sending info to player
        player.sendMessage(
                new LiteralText(
                        isVanished ?
                                "§6You are now unvanished." :
                                "§6Puff! You have vanished from the world."
                ),
                true
        );
        return 1;
    }
}
