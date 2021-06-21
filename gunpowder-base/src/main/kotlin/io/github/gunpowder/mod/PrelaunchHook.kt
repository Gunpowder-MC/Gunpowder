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

package io.github.gunpowder.mod

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.net.URL


/**
 * So you want to Mixin into Authlib/Brigadier/DataFixerUpper, on Fabric you'll need this guy.
 *
 * <p>YOU SHOULD ONLY USE THIS CLASS DURING "preLaunch" and ONLY TARGET A CLASS WHICH IS NOT ANY CLASS YOU MIXIN TO.
 *
 * This will likely not work on Gson because FabricLoader has some special logic related to Gson.
 *
 * Source: https://github.com/i509VCB/Fabric-Junkkyard/blob/master/src/main/java/me/i509/junkkyard/hacks/PreLaunchHacks.java
 */
object PrelaunchHook {
    private val KNOT_CLASSLOADER = Thread.currentThread().contextClassLoader
    private val ADD_URL_METHOD: Method

    init {
        try {
            ADD_URL_METHOD = KNOT_CLASSLOADER.javaClass.getMethod("addURL", URL::class.java)
            ADD_URL_METHOD.isAccessible = true
        } catch (e: ReflectiveOperationException) {
            throw RuntimeException("Failed to load Classloader fields", e)
        }


    }

    /**
     * Hackily load the package which a mixin may exist within.
     *
     * YOU SHOULD NOT TARGET A CLASS WHICH YOU MIXIN TO.
     *
     * @param pathOfAClass The path of any class within the package.
     */
    fun moveToKnot(pathOfAClass: String) {
        val url = Class.forName(pathOfAClass).protectionDomain.codeSource.location
        ADD_URL_METHOD.invoke(KNOT_CLASSLOADER, url)
    }
}
