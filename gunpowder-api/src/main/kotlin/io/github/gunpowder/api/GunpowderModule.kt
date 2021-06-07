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

package io.github.gunpowder.api

/**
 * Interface a registered module should implement.
 */
interface GunpowderModule {
    val name: String

    @Deprecated("Unused, will be removed in later versions")
    val toggleable: Boolean

    /**
     * Lower priority value means loaded earlier
     */
    @JvmDefault
    val priority: Int
        get() = 1000

    // The methods below are in call order

    fun registerConfigs() {}
    fun registerEvents() {}
    fun registerComponents() {}
    fun registerTables() {}
    fun registerCommands() {}

    /**
     * Called on datapack reload (aka /reload)
     *
     * Use this to e.g. clear DB caches, update lang files, etc
     */
    fun reload() {}

    /**
     * Called when initializing a module. This is called after all register methods.
     */
    fun onInitialize() {}
}
