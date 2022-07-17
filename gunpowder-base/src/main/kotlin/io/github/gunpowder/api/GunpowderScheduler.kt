package io.github.gunpowder.api

import io.github.gunpowder.api.types.CancellableTask
import io.github.gunpowder.api.types.TimeUnit

interface GunpowderScheduler {
    fun schedule(runAfter: TimeUnit = TimeUnit.NOW, interval: TimeUnit = TimeUnit.NEVER, check: () -> Boolean, onComplete: () -> Unit = {}, block: () -> Unit) : CancellableTask
}
