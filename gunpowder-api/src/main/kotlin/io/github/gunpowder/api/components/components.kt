package io.github.gunpowder.api.components

import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KVisibility
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.internal.impl.resolve.calls.inference.CapturedType

val map = WeakHashMap<Any, HashMap<Class<out Component<*>>, Component<*>>>()
val injected = HashMap<Class<*>, MutableList<Class<out Component<*>>>>()

inline fun <reified A : Component<*>> Class<*>.bind() {
    val list = injected[this] ?: mutableListOf<Class<out Component<*>>>().also { injected[this] = it }
    list.add(A::class.java)
}

inline fun <reified A : Component<*>> KClass<*>.bind() {
    val list = injected[this.java] ?: mutableListOf<Class<out Component<*>>>().also { injected[this.java] = it }
    list.add(A::class.java)
}

inline fun <reified A : Component<*>> Any.with(): A {
    return with(A::class.java)
}

@Deprecated("Used internally")
fun Class<*>.getComponents(): List<Class<out Component<*>>> {
    // Applies bound parameters to instance
    val list = mutableListOf<Class<out Component<*>>>()
    injected.filter { this.kotlin.isSubclassOf(it.key.kotlin) }.forEach {
        list.addAll(it.value)
    }
    return list
}

@Deprecated("Used internally")
fun Any.withAll(): List<Component<*>> {
    // Get all attached components

    val components = map[this] ?: return emptyList()
    return components.values.toList()
}

@Deprecated("Used internally")
fun <A : Component<*>> Any.with(component: Class<A>) : A {
    val components = map[this] ?: HashMap<Class<out Component<*>>, Component<*>>().also { map[this] = it };
    val instance = components[component] ?: let {
        val constructor = component.kotlin.constructors.firstOrNull { it.parameters.isEmpty() && it.visibility === KVisibility.PUBLIC } ?: error("No public no-args constructor on class $component!")
        val obj = constructor.call()
        obj.setBound(this)
        components[component] = obj
        obj
    }
    return instance as A
}
