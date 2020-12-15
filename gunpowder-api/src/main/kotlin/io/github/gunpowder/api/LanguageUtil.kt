package io.github.gunpowder.api

interface LanguageUtil {
    /**
     * Get a key->text mapping for a given language.
     */
    fun get(lang: String): Map<String, String>
}
