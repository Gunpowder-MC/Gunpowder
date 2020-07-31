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

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.nyliummc.essentials.mixin.cast.PlayerVanish;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ListCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Mixin(ListCommand.class)
public class ListCommandMixin_Utilities {

    @Inject(
            method = "execute(Lnet/minecraft/server/command/ServerCommandSource;Ljava/util/function/Function;)I",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/text/Texts;join(Ljava/util/Collection;Ljava/util/function/Function;)Lnet/minecraft/text/MutableText;"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true
    )
    // We should remove player from list if vanished
    private static void executeListCommand(ServerCommandSource source, Function<ServerPlayerEntity, Text> nameProvider, CallbackInfoReturnable<Integer> cir, PlayerManager playerManager, List<ServerPlayerEntity> list, Text text) {
        try {
            // If player is not vanished hidden players must be removed from list
            if (!((PlayerVanish) source.getPlayer()).isVanished()) {
                List<ServerPlayerEntity> playerList = new ArrayList<>(list);

                // Removing vanished players from list
                playerList.removeIf(playerEntity -> ((PlayerVanish) playerEntity).isVanished());

                text = Texts.join(playerList, nameProvider);

                // Sending "fake" list info
                source.sendFeedback(new TranslatableText("commands.list.players", playerList.size(), playerManager.getMaxPlayerCount(), text), false);
                cir.setReturnValue(playerList.size());
            }
        } catch (Error | CommandSyntaxException ignored) {
            // Source of the command wasn't player, we don't need to hide anything
        }
    }
}
