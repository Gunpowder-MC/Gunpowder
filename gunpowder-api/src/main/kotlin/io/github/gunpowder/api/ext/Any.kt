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
