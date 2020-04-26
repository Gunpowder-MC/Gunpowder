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
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.sql.Connection
import io.github.nyliummc.essentials.api.EssentialsDatabase as APIEssentialsDatabase

object EssentialsDatabase : APIEssentialsDatabase {
    fun disconnect() {
        TransactionManager.closeAndUnregister(db)
    }

    private val config = EssentialsMod.instance!!.registry.getConfig(EssentialsConfig::class.java)

    private var mode = config.database.mode
    private val host = config.database.host
    private val port = config.database.port
    private val databaseUser = config.database.username
    private val databasePassword = config.database.password
    override lateinit var db: Database

    // Not configurable
    private val databaseName = "essentials"

    fun loadDatabase() {
        if (EssentialsMod.instance!!.isClient) {
            this.mode = "sqlite"
        }

        when (this.mode) {
            "sqlite" -> {
                val path = EssentialsMod.instance!!.server.runDirectory.canonicalPath

                db = Database.connect(
                        "jdbc:sqlite:$path/essentials.db",
                        "org.sqlite.JDBC")

                // Patch for SQLite
                TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
            }
            "postgres" -> {
                db = Database.connect(
                        "jdbc:postgresql://$host:$port/$databaseName",
                        "org.postgresql.Driver",
                        databaseUser,
                        databasePassword)
            }
            "mysql" -> {
                db = Database.connect(
                        "jdbc:mysql://$host:$port/$databaseName",
                        "com.mysql.jdbc.Driver",
                        databaseUser,
                        databasePassword)
            }
            else -> {
                println("$mode invalid")
                throw AssertionError("Invalid db type")
            }
        }
    }
}
