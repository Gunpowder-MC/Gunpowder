package io.github.gunpowder.api

import java.util.concurrent.CompletableFuture

import org.jetbrains.exposed.sql.Transaction
import java.util.function.Consumer

interface GunpowderDatabase {
    fun transaction(block: Consumer<Transaction>)
    fun <T> transaction(block: Transaction.() -> T): CompletableFuture<T>
    fun <T> transactionWait(block: Transaction.() -> T): T
}
