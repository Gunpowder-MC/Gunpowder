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