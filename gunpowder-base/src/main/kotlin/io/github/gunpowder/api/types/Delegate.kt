package io.github.gunpowder.api.types

import kotlin.reflect.KProperty

fun interface Delegate<T> {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T
}
