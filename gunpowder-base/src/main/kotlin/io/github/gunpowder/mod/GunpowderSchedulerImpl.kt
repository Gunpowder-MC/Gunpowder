package io.github.gunpowder.mod

import com.martmists.commons.logging.logger
import io.github.gunpowder.api.GunpowderScheduler
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import java.time.LocalDateTime
import java.time.temporal.UnsupportedTemporalTypeException
import java.util.concurrent.PriorityBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class GunpowderSchedulerImpl(private val poolSize: Int) : GunpowderScheduler {
    @Volatile
    private var running = false
    private val schedule = PriorityBlockingQueue<Entry>(100) { a, b ->
        a.runAt.compareTo(b.runAt)
    }
    private val log by logger()
    private lateinit var pool: List<Thread>

    data class Entry(val task: Task, val runAt: LocalDateTime)
    data class Task(val interval: io.github.gunpowder.api.types.TimeUnit, val check: () -> Boolean, val onComplete: () -> Unit, val block: () -> Unit)

    private fun createPool() = List(poolSize) {
        thread(start = false, isDaemon = true, name = "Gunpowder Scheduler Thread #$it") {
            while (running) {
                try {
                    // Poll for an available task
                    val entry = schedule.poll(1, TimeUnit.SECONDS)

                    if (entry != null) {
                        val task = entry.task
                        if (entry.runAt < LocalDateTime.now()) {
                            val shouldRun = try {
                                // Check if the task is still valid
                                task.check()
                            } catch (e: Exception) {
                                log.error("An error occurred checking if a scheduled task should run", e)
                                continue
                            }

                            if (shouldRun) {
                                val success = try {
                                    task.block()
                                    true
                                } catch (e: Exception) {
                                    log.error("An error occurred running a scheduled task", e)
                                    false
                                }

                                if (success) {
                                    // Schedule the task again because it didn't error
                                    try {
                                        schedule.add(
                                            Entry(
                                                task,
                                                entry.runAt.plus(task.interval.value, task.interval.unit)
                                            )
                                        )
                                    } catch (e: UnsupportedTemporalTypeException) {
                                        // ChronoUnit.FOREVER or similar, ignore because it shouldn't run again
                                    }
                                }
                            } else {
                                task.onComplete()
                            }
                        } else {
                            // This task shouldn't run yet, add it back to the queue
                            schedule.add(entry)
                        }
                    }
                } catch (e: InterruptedException) {
                    break
                }
            }
        }
    }

    private fun start() {
        running = true
        pool = createPool()
        for (thread in pool) {
            thread.start()
        }
    }

    private fun stop() {
        running = false
        for (thread in pool) {
            thread.interrupt()
        }
        schedule.clear()
    }

    override fun schedule(
        runAfter: io.github.gunpowder.api.types.TimeUnit,
        interval: io.github.gunpowder.api.types.TimeUnit,
        check: () -> Boolean,
        onComplete: () -> Unit,
        block: () -> Unit
    ) {
        schedule.add(Entry(Task(interval, check, onComplete, block), LocalDateTime.now().plus(runAfter.value, runAfter.unit)))
    }

    fun setupListeners() {
        ServerLifecycleEvents.SERVER_STARTED.register {
            start()
        }

        ServerLifecycleEvents.SERVER_STOPPED.register {
            stop()
        }
    }
}
