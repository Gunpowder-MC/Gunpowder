package io.github.nyliummc.essentials.api

import com.mojang.brigadier.CommandDispatcher
import io.github.nyliummc.essentials.api.builders.Command
import net.minecraft.server.command.ServerCommandSource
import org.jetbrains.exposed.sql.Table
import java.util.function.Supplier
import kotlin.reflect.KClass

interface EssentialsRegistry {
    fun registerCommand(callback: (CommandDispatcher<ServerCommandSource>) -> Unit)
    fun registerCommand(callback: (Command, CommandDispatcher<ServerCommandSource>) -> Unit)

    fun <T> getBuilder(clz: Class<T>): T
    fun <T> getModelHandler(clz: Class<T>): T

    fun <T> registerBuilder(clz: Class<T>, supplier: Supplier<T>)
    fun <T> registerModelHandler(clz: Class<T>, supplier: Supplier<T>)

    fun registerTable(tab: Table)
}
