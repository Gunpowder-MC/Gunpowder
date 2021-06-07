package io.github.gunpowder.api.components

import java.util.*
import kotlin.reflect.KVisibility

val map = WeakHashMap<Any, HashMap<Class<out Component>, out Component>>()
val injected = HashMap<Class<*>, MutableList<Class<out Component>>>()

inline fun <reified A : Component> Class<*>.bind() {
    val list = injected[this] ?: mutableListOf<Class<out Component>>().also { injected[this] = it }
    list.add(A::class.java)
}

inline fun <reified A : Component> Any.with(): A {
    return with(A::class.java)
}

@Deprecated("Used internally")
fun Class<*>.getComponents(): List<Class<out Component>> {
    // Applies bound parameters to instance
    return injected[this] ?: emptyList()
}

@Deprecated("Used internally")
fun Any.withAll(): List<Component> {
    // Get all attached components

    val components = map[this] ?: return emptyList()
    return components.values.toList()
}

@Deprecated("Used internally")
fun <A : Component> Any.with(component: Class<A>) : A {
    val components = map[this] ?: HashMap<Class<out Component>, Component>().also { map[this] = it };
    val instance = components[component] ?: let {
        val constructor = component.kotlin.constructors.firstOrNull { it.parameters.isEmpty() && it.visibility === KVisibility.PUBLIC } ?: error("No public no-args constructor on class $component!")
        constructor.call()
    }
    return instance as A
}
