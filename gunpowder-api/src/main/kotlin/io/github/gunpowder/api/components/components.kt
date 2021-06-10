package io.github.gunpowder.api.components

import org.jetbrains.annotations.ApiStatus
import java.lang.IllegalArgumentException
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KVisibility
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.internal.impl.resolve.calls.inference.CapturedType

inline fun <reified C : Component<*>> Class<*>.bind() {
    val list = injected.getOrPut(this) { mutableListOf() }
    list.add(C::class.java)
}

inline fun <reified C : Component<*>> KClass<*>.bind() = this.java.bind<C>()

inline fun <reified C : Component<*>> Any.with(): C {
    return with(C::class.java)
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
