package io.github.nyliummc.essentials.api

import com.mojang.brigadier.CommandDispatcher
import io.github.nyliummc.essentials.api.extension.EssentialsExtension
import io.github.nyliummc.essentials.api.util.Builder
import net.minecraft.server.command.ServerCommandSource
import java.util.function.Supplier

interface EssentialsRegistry {
    fun <T, B : Builder<T>> supplyBuilder(builderClass: Class<B>): B
    fun <T, B : Builder<T>> registerBuilder(builderClass: Class<B>, builderSupplier: Supplier<B>)
    fun <T : EssentialsExtension> registerExtension(extensionClass: Class<T>, instance: T)
    fun registerCommand(registerCallback: (CommandDispatcher<ServerCommandSource>) -> Unit)
}