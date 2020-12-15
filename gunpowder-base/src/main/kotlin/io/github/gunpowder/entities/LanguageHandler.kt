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
import net.minecraft.util.Language
import java.io.IOException


object LanguageHandler : LanguageUtil {
    private val mapping = mutableMapOf<String, Map<String, String>>()

    fun loadLanguage(lang: String): Map<String, String> {
        val builder = ImmutableMap.builder<String, String>()

        FabricLoader.getInstance().allMods.filter { itt: ModContainer ->
            itt.metadata.depends.stream().anyMatch { it: ModDependency -> it.modId == "gunpowder-base" }
        }.forEach { c: ModContainer ->
            try {
                val inputStream =
                    Language::class.java.getResourceAsStream(
                        "/assets/${c.metadata.id}/lang/${lang}.json"
                    )

                var prevErr: Throwable? = null

                try {
                    Language.load(inputStream, builder::put)
                } catch (err: Throwable) {
                    prevErr = err
                    throw err
                } finally {
                    if (inputStream != null) {
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
                }

            } catch (err: JsonParseException) {
                instance.logger.error(
                    "Couldn't read strings from /assets/${c.metadata.id}/lang/${lang}.json", err
                )
            } catch (err: IOException) {
                instance.logger.error(
                    "Couldn't read strings from /assets/${c.metadata.id}/lang/${lang}.json", err
                )
            }
        }

        return builder.build()
    }

    override fun get(lang: String): Map<String, String> {
        return mapping.getOrPut(lang) { loadLanguage(lang) }
    }
}
