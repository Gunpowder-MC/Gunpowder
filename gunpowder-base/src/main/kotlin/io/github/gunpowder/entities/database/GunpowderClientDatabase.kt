package io.github.gunpowder.entities.database

import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.util.WorldSavePath
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.sql.Connection

object GunpowderClientDatabase : AbstractDatabase() {
    override lateinit var db: Database

    override fun loadDatabase() {
        var path = FabricLoader.getInstance().gameDir.toFile().canonicalPath
        val filename = MinecraftClient.getInstance().server?.getSavePath(WorldSavePath.ROOT)?.fileName
            ?: return
        path += "/$filename"

        db = Database.connect(
            "jdbc:sqlite:$path/gunpowder.db",
            "org.sqlite.JDBC")
        // Patch for SQLite
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE

        super.loadDatabase()
    }
}

