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

package io.github.nyliummc.essentials.configs

data class CustomHead(
        val id: String,
        val name: String,
        val uuid: String,
        val url: String,
        val nbt: String?
)

data class VanillaTweaksConfig(
        val creeperExplosionStrength: Int,
        val ghastExplosionStrength: Int,
        val allowEndermanPickup: Boolean,
        val silenceNametagValues: List<String>,
        val playersDropHeads: Boolean,
        val mobHeadChance: Int,
        val customHeads: List<CustomHead>
)

data class VoodooConfig(
        val shulkermites: Boolean,
        val silentWither: Boolean,
        val safeDoors: Boolean,
        val autoSaplings: Boolean,
        val apiaristSuit: Boolean,
        val invisibleItemFrames: Boolean,
        val netheriteFireImmune: Boolean
)

data class DatapacksConfig(
        val vanillaTweaks: VanillaTweaksConfig,
        val voodooBeard: VoodooConfig
)
