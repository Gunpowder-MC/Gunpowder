package io.github.gunpowder.api.ext

import io.github.gunpowder.api.GunpowderScheduler
import io.github.gunpowder.api.types.Delegate
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.reflect.KProperty

private class LazyScheduledImpl<T>(provider: () -> T) : Delegate<T>, KoinComponent {
    private var value: T? = null
    private val scheduler by inject<GunpowderScheduler>()
    private val task = scheduler.schedule {
        value = provider()
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        task.get()
        return value as T
    }

}

fun <T> lazyScheduled(provider: () -> T) : Delegate<T> {
    return LazyScheduledImpl(provider)
}
