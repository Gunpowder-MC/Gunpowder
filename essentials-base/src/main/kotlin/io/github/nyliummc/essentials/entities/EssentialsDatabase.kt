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

import io.github.nyliummc.essentials.api.EssentialsDatabase as APIEssentialsDatabase
import io.github.nyliummc.essentials.api.EssentialsMod
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.lang.AssertionError
import java.sql.Connection

object EssentialsDatabase : APIEssentialsDatabase {
    // TODO: Configurable
    private var mode = "sqlite"
    private val host = "localhost"
    private val port = if (mode == "postgres") 5432 else 3306  // Default ports for postgres/mysql
    private val databaseUser = "db_user"
    private val databasePassword = "db_password"

    // Not configurable
    private val databaseName = "essentials"

    override val db by lazy {
        if (EssentialsMod.instance!!.isClient) {
            this.mode = "sqlite"
        }

        when (mode) {
            "sqlite" -> {
                val path = EssentialsMod.instance!!.server.runDirectory.absolutePath
                System.out.println(path)
                TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
                Database.connect(
                        "jdbc:sqlite:$path/essentials.db",
                        "org.sqlite.JDBC")
            }
            "postgres" -> {
                Database.connect(
                        "jdbc:postgresql://$host:$port/$databaseName",
                        "org.postgresql.Driver",
                        databaseUser,
                        databasePassword)
            }
            "mysql" -> {
                Database.connect(
                        "jdbc:mysql://$host:$port/$databaseName",
                        "com.mysql.jdbc.Driver",
                        databaseUser,
                        databasePassword)
            }
            else -> throw AssertionError("Invalid db type")
        }
    }
}