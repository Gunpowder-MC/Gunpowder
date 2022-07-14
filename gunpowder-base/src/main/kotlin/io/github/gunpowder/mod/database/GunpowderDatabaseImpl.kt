package io.github.gunpowder.mod.database

import io.github.gunpowder.api.GunpowderDatabase
import com.martmists.commons.database.ThreadedDatabase
import io.github.gunpowder.api.events.DatabaseEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

abstract class GunpowderDatabaseImpl : GunpowderDatabase {
    abstract fun getDatabase(): Database
    private lateinit var connection: ThreadedDatabase

    override fun transaction(block: Consumer<Transaction>) {
        transaction(block::accept)
    }

    override fun <T> transaction(block: Transaction.() -> T): CompletableFuture<T> {
        return connection.transaction(block)
    }

    override fun <T> transactionWait(block: Transaction.() -> T): T {
        return transaction(block).get()
    }

    fun setupListeners() {
        ServerLifecycleEvents.SERVER_STARTING.register {
            connection = ThreadedDatabase(createDb=::getDatabase)
            DatabaseEvents.DATABASE_READY.invoker().trigger()
        }

        ServerLifecycleEvents.SERVER_STOPPING.register {
            connection.close()
            DatabaseEvents.DATABASE_CLOSED.invoker().trigger()
        }
    }
}
