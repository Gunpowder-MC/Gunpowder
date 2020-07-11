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

package io.github.nyliummc.essentials.mixin.chat;

import io.github.nyliummc.essentials.api.EssentialsMod;
import io.github.nyliummc.essentials.configs.ChatConfig;
import io.github.nyliummc.essentials.entities.TextFormatter;
import net.minecraft.text.LiteralText;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LiteralText.class)
public abstract class LiteralTextMixin_Chat {
    @Mutable
    @Final
    @Shadow
    private String string;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void applyFormattingToLiteralText(String string, CallbackInfo ci) {
        if (string == null) {
            return; // lol no, not our problem
        }

        try {
            if (EssentialsMod.getInstance().getRegistry().getConfig(ChatConfig.class).getEnableChatColors()) {
                this.string = TextFormatter.INSTANCE.formatString(string);
            } else {
                this.string = string;
            }
        } catch (Throwable ignored) {
            // TODO: Please, this is a nightmare since there is text constructed before essentials is even started. We simply cannot handle mixins into LiteralText.
            // Why? Well the server does construct some literal text before an essentials instance is injected into EssentialsProvider.
        }
    }
}
