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

package io.github.gunpowder.api.ext

//import eu.pb4.permissions.api.v0.*
//import eu.pb4.permissions.impl.VanillaPermissionProvider
import net.minecraft.server.network.ServerPlayerEntity

/**
 * Get a property.
 * For `node.[int]` this will be an Int
 * for `node.[double]` this will be a Double
 * for regular permission nodes this will be a Boolean
 */
fun <T> ServerPlayerEntity.getPermission(node: String, default: T) : T {
    return default
//    val adapter = when (default) {
//        is Int -> ValueAdapter.INTEGER
//        is Double -> ValueAdapter.DOUBLE
//        is Boolean -> {
//            return (Permissions.get().check(UserContext.of(this), node).toBoolean(default)) as T
//        }
//        else -> error("Unknown permission value type")
//    }
//    return PermissionProvider.getValueFrom(listOf(node), default, adapter as ValueAdapter<T>)
}

/**
 * Same as above, but uses `fallback` if the provider is the builtin provider (aka not present)
 */
fun <T> ServerPlayerEntity.getPresentPermission(node: String, default: T, fallback: T) : T {
    return fallback
//    if (Permissions.get() is VanillaPermissionProvider) {
//        return fallback
//    }
//    return getPermission(node, default)
}

fun ServerPlayerEntity.getPresentPermission(node: String, opLevel: Int) = getPresentPermission(node, false, this.hasPermissionLevel(opLevel))
