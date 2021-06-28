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

import io.github.gunpowder.api.GunpowderMod
import io.github.gunpowder.configs.GunpowderConfig
import net.fabricmc.loader.api.FabricLoader
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.sql.Connection
import java.util.concurrent.CompletableFuture
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import io.github.gunpowder.api.GunpowderDatabase as APIGunpowderDatabase
import org.jetbrains.exposed.sql.transactions.transaction as dbTransaction

object GunpowderServerDatabase : AbstractDatabase() {
    private val config by lazy { GunpowderMod.instance.registry.getConfig(GunpowderConfig::class.java) }
    override lateinit var db: Database

    override fun loadDatabase() {
        val dbc = config.database
        val mode = dbc.mode
        val databaseName = config.database.database

        when (mode) {
            "sqlite" -> {
                val path = FabricLoader.getInstance().gameDir.toFile().canonicalPath

                db = Database.connect(
                        "jdbc:sqlite:$path/gunpowder.db",
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
                GunpowderMod.instance.logger.error("DB mode '$mode' is invalid")
                throw AssertionError("Invalid db type")
            }
        }

        super.loadDatabase()
    }
}
