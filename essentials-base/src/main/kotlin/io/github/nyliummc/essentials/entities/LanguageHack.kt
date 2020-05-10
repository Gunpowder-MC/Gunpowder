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

package io.github.nyliummc.essentials.entities

import com.google.gson.Gson
import com.google.gson.JsonObject
import io.github.nyliummc.essentials.mixin.base.LanguageAccessor_Base
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.util.JsonHelper
import net.minecraft.util.Language
import org.apache.logging.log4j.core.util.Closer
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.nio.file.Files

// Loads lang files on server
object LanguageHack {
    fun activate(modid: String) {
        val language = Language.getInstance() as LanguageAccessor_Base
        val inputStream = Files.newInputStream(
                FabricLoader.getInstance()
                        .getModContainer(modid).get()
                        .getPath("assets/$modid/lang/en_us.json")
        )

        try {
            val jsonObject = Gson().fromJson(
                    InputStreamReader(inputStream, StandardCharsets.UTF_8),
                    JsonObject::class.java
            )

            jsonObject.entrySet().forEach { entry ->
                val string = language.field_11489
                        .matcher(JsonHelper.asString(entry.value, entry.key))
                        .replaceAll("%$1s")
                language.translations[entry.key] = string
            }
        } finally {
            Closer.closeSilently(inputStream)
        }
    }
}
