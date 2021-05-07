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

import java.util.*

private val map = WeakHashMap<Any, MutableMap<String, Any>>()

/**
 * Used to set custom properties on objects.
 * Note that these are not serialized anywhere, and disappear when the referenced object is removed.
 * This means that adding to e.g. a UUID, the UUID leaves memory, and another same UUID is created,
 *  it loses the property.
 */
operator fun <T> Any.set(name: String, value: T) {
    map.getOrPut(this) { mutableMapOf() }[name] = value as Any
}

operator fun <T> Any.get(name: String) : T {
    return getOrNull<T>(name) ?: error("Property $name was null!")
}

fun <T> Any.getOrNull(name: String) : T? {
    return map[this]?.get(name) as T?
}
