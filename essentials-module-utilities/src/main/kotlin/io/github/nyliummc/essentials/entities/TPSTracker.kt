package io.github.nyliummc.essentials.entities

import java.lang.Double.min

class TPSTracker(val id: String) {
    private var lastTime = System.nanoTime()
    private val history = mutableListOf<Double>()
    private val msptHistory = mutableListOf<Long>()
    private val intervalTicks = 1000 / 20  // max tps = 20

    fun tick() {
        val startTime = System.nanoTime()
        var timeSpent = (startTime - lastTime) / 1000  // Millis
        if (timeSpent == 0L) {
            timeSpent = 1
        }
        if (history.size > 1200) {  // Average over the last minute
            history.removeAt(0)
            msptHistory.removeAt(0)
        }
        val tps = intervalTicks * 1_000_000.0 / timeSpent
        history.add(min(tps, 20.0))
        msptHistory.add(timeSpent)
        lastTime = startTime
    }

    fun getTps(): Double = history.average()
    fun getMspt(): Double = history.average()
}
