package io.github.nyliummc.essentials.api

import com.mojang.brigadier.CommandDispatcher
import io.github.nyliummc.essentials.api.builders.Command
import net.minecraft.server.command.ServerCommandSource
import java.util.function.Supplier
import kotlin.reflect.KClass

interface EssentialsRegistry {
    fun registerCommand(callback: (CommandDispatcher<ServerCommandSource>) -> Unit)
    fun registerCommand(callback: (Command, CommandDispatcher<ServerCommandSource>) -> Unit)

    fun <T> getBuilder(clz: Class<T>): T
}