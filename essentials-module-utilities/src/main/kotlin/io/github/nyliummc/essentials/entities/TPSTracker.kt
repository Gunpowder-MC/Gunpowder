/*
 * MIT License
 *
 * Copyright (c) NyliumMC
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
        msptHistory.add(timeSpent / 1000)
        lastTime = startTime
    }

    fun getTps(): Double = history.average()
    fun getMspt(): Double = msptHistory.average()
}
