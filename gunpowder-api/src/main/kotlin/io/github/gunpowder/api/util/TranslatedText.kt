package io.github.gunpowder.api.util

import io.github.gunpowder.api.GunpowderMod
import net.minecraft.text.*

class TranslatedText(val key: String, vararg val args: Any?) {
    /**
     * Translate the TranslatedText to a string
     */
    fun translate(languageCode: String): String {
        val langMap = GunpowderMod.instance.languageEngine.get(languageCode)
        return langMap.getOrDefault(key, key).format(*args)
    }

    /**
     * Translate the TranslatedText to a TranslatableText
     */
    fun translateText(languageCode: String): TranslatableText {
        return TranslatableText(translate(languageCode), args)
    }
}
