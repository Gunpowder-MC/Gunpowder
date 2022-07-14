package io.github.gunpowder.api.types

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit

data class TimeUnit(val value: Long, val unit: TemporalUnit) {
    companion object {
        val NOW = TimeUnit(0, ChronoUnit.NANOS)
        val NEVER = TimeUnit(1, ChronoUnit.FOREVER)
    }
}

operator fun LocalDateTime.plus(unit: TimeUnit) = this.plus(unit.value, unit.unit)
