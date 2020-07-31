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
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

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

    //todo server ping -> players; hide vanished
    private static int toggleVanish(ServerCommandSource src) throws CommandSyntaxException {
        PlayerVanish player = (PlayerVanish) src.getPlayer();

        player.setVanished(!player.isVanished());
        // Sending info to player
        src.getPlayer().sendMessage(
                new LiteralText(
                        player.isVanished() ?
                                "§6Puff! You have vanished from the world.":
                                "§6You are now unvanished."
                ),
                true
        );
        return 1;
    }
}
