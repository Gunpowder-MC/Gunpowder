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

package io.github.gunpowder.api.components

import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KVisibility

inline fun <reified C : Component<*>> Class<*>.bind() {
    val list = injected.getOrPut(this) { mutableListOf() }
    list.add(C::class.java)
}

inline fun <reified C : Component<*>> KClass<*>.bind() = this.java.bind<C>()

inline fun <reified C : Component<*>> Any.with(): C {
    return with(C::class.java)
}

inline fun <reified C: Component<*>> Any.with(block: C.() -> Unit) {
    block(with())
}

// ---- Internals below ----

val map = WeakHashMap<Any, MutableMap<Class<out Component<*>>, Component<*>>>()
val injected = mutableMapOf<Class<*>, MutableList<Class<out Component<*>>>>()

//@ApiStatus.Internal
fun Class<*>.getComponents(): List<Class<out Component<*>>> {
    // Applies bound parameters to instance
    val list = mutableListOf<Class<out Component<*>>>()
    injected.filter { it.key.isAssignableFrom(this) }.forEach {
        list.addAll(it.value)
    }
    return list
}

//@ApiStatus.Internal
fun Any.withAll(): List<Component<*>> {
    // Get all attached components

    val components = map[this] ?: return emptyList()
    return components.values.toList()
}

//@ApiStatus.Internal
fun <A : Component<*>> Any.with(component: Class<A>) : A {
    if (!this::class.java.getComponents().contains(component)) {
        throw IllegalArgumentException("Component ${component.name} not bound to class ${this::class.java.name}")
    }

    val components = map.getOrPut(this) { mutableMapOf() }
    val instance = components.getOrPut(component) {
        val constructor = component.kotlin.constructors.firstOrNull { it.parameters.isEmpty() && it.visibility === KVisibility.PUBLIC } ?: error("No public no-args constructor on component ${component.name}!")
        val obj = constructor.call()
        obj.setBound(this)
        obj
    }
    return instance as A
}
