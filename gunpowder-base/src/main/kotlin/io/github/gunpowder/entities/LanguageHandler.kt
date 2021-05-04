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

package io.github.gunpowder.entities

import com.google.common.collect.ImmutableMap
import com.google.gson.JsonParseException
import io.github.gunpowder.api.GunpowderMod.Companion.instance
import io.github.gunpowder.api.LanguageUtil
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer
import net.fabricmc.loader.api.metadata.ModDependency
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Language
import java.io.IOException
import java.util.*


object LanguageHandler : LanguageUtil {

    // TODO: Allow mods to register custom folders
    val modPath = mutableMapOf<String, String>()

    private val mapping = mutableMapOf<String, Map<String, String>>()
    val languageSettings = WeakHashMap<UUID, String>()

    fun tryLoad(id: String, lang: String, consumer: (String, String) -> Unit) {
        val path = modPath.getOrDefault(id, "/assets/$id/lang")

        try {
            val inputStream =
                Language::class.java.getResourceAsStream(
                    "$path/$lang.json"
                ) ?: return

            var prevErr: Throwable? = null

            try {
                Language.load(inputStream, consumer)
            } catch (err: Throwable) {
                prevErr = err
                throw err
            } finally {
                if (prevErr != null) {
                    try {
                        inputStream.close()
                    } catch (err: Throwable) {
                        prevErr.addSuppressed(err)
                    }
                } else {
                    inputStream.close()
                }
            }

        } catch (err: JsonParseException) {
            instance.logger.error(
                "Couldn't read strings from /assets/$id/lang/$lang.json", err
            )
        } catch (err: IOException) {
            instance.logger.error(
                "Couldn't read strings from /assets/$id/lang/$lang.json", err
            )
        }
    }

    fun loadLanguage(lang: String): Map<String, String> {
        val map = mutableMapOf<String, String>()

        FabricLoader.getInstance().allMods.forEach { c: ModContainer ->
            tryLoad(c.metadata.id, lang, map::put)
        }

        return Collections.unmodifiableMap(map)
    }

    override fun languageForPlayer(player: ServerPlayerEntity): String {
        return languageSettings.getOrDefault(player.uuid, "en_us")
    }

    override fun get(lang: String): Map<String, String> {
        return mapping.getOrPut(lang) { loadLanguage(lang) }
    }
}
