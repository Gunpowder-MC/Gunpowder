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

package io.github.gunpowder.api.util

import io.github.gunpowder.api.GunpowderMod
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.*

class TranslatedText(val key: String, vararg val args: Any?) {
    /**
     * Translate the TranslatedText to a string
     */
    fun translate(languageCode: String): String {
        val langMap = GunpowderMod.instance.languageEngine.get(languageCode)
        return langMap.getOrDefault(key, GunpowderMod.instance.languageEngine.get("en_us").getOrDefault(key, key)).format(*args)
    }

    /**
     * Translate the TranslatedText to a TranslatableText
     */
    fun translateText(languageCode: String): TranslatableText {
        return TranslatableText(translate(languageCode), args)
    }

    /**
     * Translate for a specific player with their settings (String)
     */
    fun translateForPlayer(player: ServerPlayerEntity): String {
        return translate(GunpowderMod.instance.languageEngine.languageForPlayer(player))
    }

    /**
     * Translate for a specific player with their settings (Text)
     */
    fun translateTextForPlayer(player: ServerPlayerEntity): TranslatableText {
        return translateText(GunpowderMod.instance.languageEngine.languageForPlayer(player))
    }
}
