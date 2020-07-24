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

import io.github.nyliummc.essentials.api.EssentialsMod
import io.github.nyliummc.essentials.configs.EssentialsConfig
import it.unimi.dsi.fastutil.objects.ObjectLists
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction as dbTransaction
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.sql.Connection
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.function.Supplier
import kotlin.concurrent.thread
import io.github.nyliummc.essentials.api.EssentialsDatabase as APIEssentialsDatabase

object EssentialsDatabase : APIEssentialsDatabase {
    @Volatile private var running = true
    private val queue = ConcurrentLinkedQueue<Pair<Transaction.() -> Any, CompletableFuture<Any>>>()
    private val databaseThread = thread(start=true, isDaemon=false, name="Essentials Database Thread") {
        while (running) {
            val pair = queue.poll()
            if (pair == null) {
                Thread.sleep(20)  // 20ms to not lag thread
                continue
            }
            println("Database thread got a task")
            val value = dbTransaction {  // Because recursion
                println("Calling function")
                val x = pair.first.invoke(this)
                println("Function done")
                x
            }
            println("Setting future ${pair.second.hashCode()}")
            pair.second.complete(value)
        }
    }

    private val config by lazy { EssentialsMod.instance.registry.getConfig(EssentialsConfig::class.java) }
    override lateinit var db: Database

    // Not configurable
    private val databaseName = "essentials"

    fun disconnect() {
        running = false
        databaseThread.join()
        try {
            TransactionManager.closeAndUnregister(db)
        } catch (e: UninitializedPropertyAccessException) {
            // Ignore; Server closed before DB was created, no issues here
        }
    }

    fun loadDatabase() {
        val dbc = config.database
        var mode = dbc.mode

        if (EssentialsMod.instance.isClient) {
            mode = "sqlite"
        }

        when (mode) {
            "sqlite" -> {
                val path = EssentialsMod.instance.server.runDirectory.canonicalPath

                db = Database.connect(
                        "jdbc:sqlite:$path/essentials.db",
                        "org.sqlite.JDBC")

                // Patch for SQLite
                TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
            }
            "postgres" -> {
                db = Database.connect(
                        "jdbc:postgresql://${dbc.host}:${dbc.port}/$databaseName",
                        "org.postgresql.Driver",
                        dbc.username,
                        dbc.password)
            }
            "mysql" -> {
                db = Database.connect(
                        "jdbc:mysql://${dbc.host}:${dbc.port}/$databaseName",
                        "com.mysql.jdbc.Driver",
                        dbc.username,
                        dbc.password)
            }
            else -> {
                println("$mode invalid")
                throw AssertionError("Invalid db type")
            }
        }
    }

    override fun <T> transaction(callback: Transaction.() -> T): CompletableFuture<T> {
        val fut = CompletableFuture<T>()
        println("Waiting for future ${fut.hashCode()}")
        queue.add(Pair(callback as Transaction.() -> Any, fut as CompletableFuture<Any>))
        return fut
    }
}
