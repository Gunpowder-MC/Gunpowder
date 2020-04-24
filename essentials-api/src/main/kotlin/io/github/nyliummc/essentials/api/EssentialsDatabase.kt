package io.github.nyliummc.essentials.api

import org.jetbrains.exposed.sql.Database

interface EssentialsDatabase {
    val db: Database
}