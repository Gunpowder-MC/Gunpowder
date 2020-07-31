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
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PlayerListS2CPacket.class)
public class PlayerListS2CPacketMixin_Utilities {
    @Shadow
    @Final
    private List<PlayerListS2CPacket.Entry> entries;

    @Shadow
    private PlayerListS2CPacket.Action action;

    @Inject(method = "write(Lnet/minecraft/network/PacketByteBuf;)V", at = @At(value = "HEAD"))
    void skipVanished(PacketByteBuf buf, CallbackInfo ci) {
        if (this.entries.iterator().hasNext() && this.action.equals(PlayerListS2CPacket.Action.ADD_PLAYER)) {
            this.entries.removeIf(entry ->
                ((PlayerVanish) EssentialsMod.getInstance().getServer().getPlayerManager().getPlayer(entry.getProfile().getId())).isVanished()
            );
        }

    }
}
