package io.github.nyliummc.essentials.entities

data class DatabaseConfig(
        val mode: String,
        val host: String,
        val port: Int,
        val username: String,
        val password: String
)

data class EssentialsConfig(
        val database: DatabaseConfig
)
