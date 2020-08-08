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

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonParseException;
import io.github.gunpowder.api.GunpowderMod;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Language;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

@Mixin(Language.class)
public abstract class LanguageMixin_Base {
    @Shadow
    @Final
    private static Pattern TOKEN_PATTERN;

    @Redirect(method = "create", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap$Builder;build()Lcom/google/common/collect/ImmutableMap;"))
    private static ImmutableMap storeTranslations(ImmutableMap.Builder builder) {
        BiConsumer biConsumer = builder::put;

        FabricLoader.getInstance().getAllMods().stream().filter(itt -> itt.getMetadata().getDepends().stream().anyMatch(it -> it.getModId().equals("gunpowder-base"))).forEach(c -> {
            try {
                InputStream inputStream = Language.class.getResourceAsStream("/assets/" + c.getMetadata().getId() + "/lang/en_us.json");
                Throwable prevErr = null;

                try {
                    Language.load(inputStream, biConsumer);
                } catch (Throwable err) {
                    prevErr = err;
                    throw err;
                } finally {
                    if (inputStream != null) {
                        if (prevErr != null) {
                            try {
                                inputStream.close();
                            } catch (Throwable err) {
                                prevErr.addSuppressed(err);
                            }
                        } else {
                            inputStream.close();
                        }
                    }

                }
            } catch (JsonParseException | IOException err) {
                GunpowderMod.getInstance().getLogger().error("Couldn't read strings from /assets/" + c.getMetadata().getId() + "/lang/en_us.json", err);
            }
        });

        return builder.build();
    }
}
