/*
 * MIT License
 *
 * Copyright (c) GunpowderMC
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

package io.github.gunpowder.entities.database

import io.github.gunpowder.api.GunpowderDatabase
import io.github.gunpowder.api.GunpowderMod
import io.github.gunpowder.configs.GunpowderConfig
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.util.concurrent.CompletableFuture
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import org.jetbrains.exposed.sql.transactions.transaction as dbTransaction

abstract class AbstractDatabase : GunpowderDatabase {
    private val queue = LinkedBlockingQueue<Pair<Transaction.() -> Any, CompletableFuture<Any>>>()
    private val config by lazy { GunpowderMod.instance.registry.getConfig(GunpowderConfig::class.java) }
    private lateinit var databaseThread: Thread
    private var running = true

    open fun disconnect() {
        running = false
        try {
            TransactionManager.closeAndUnregister(db)
            databaseThread.join()
        } catch (e: UninitializedPropertyAccessException) {
            // Ignore; Server closed before DB was created, no issues here
        }
    }

    open fun loadDatabase() {
        running = true
        createThread()
        databaseThread.start()
    }

    private fun createThread() {
        databaseThread = thread(start = false, name = "Gunpowder Database Thread", isDaemon = true) {
            while (running) {
                val pair = queue.poll(20, TimeUnit.MILLISECONDS) ?: continue

                try {
                    val value = dbTransaction(db) {  // Because recursion
                        val x = pair.first.invoke(this)
                        x
                    }

                    pair.second.complete(value)
                } catch(e: Exception) {
                    GunpowderMod.instance.logger.error("Error on Database Thread! Please report to the mod author if this is unexpected!", e)
                    pair.second.completeExceptionally(e)
                }
            }
        }
    }

    override fun <T> transaction(callback: Transaction.() -> T): CompletableFuture<T> {
        val fut = CompletableFuture<T>()
        queue.add(Pair(callback as Transaction.() -> Any, fut as CompletableFuture<Any>))
        return fut
    }
}
