package io.github.nyliummc.essentials.entities

import com.mojang.brigadier.CommandDispatcher
import io.github.nyliummc.essentials.api.builders.Text as APIText
import io.github.nyliummc.essentials.api.builders.Command as APICommand
import io.github.nyliummc.essentials.api.builders.TeleportRequest as APITeleportRequest
import io.github.nyliummc.essentials.commands.InfoCommand
import io.github.nyliummc.essentials.entities.builders.Command
import io.github.nyliummc.essentials.entities.builders.TeleportRequest
import io.github.nyliummc.essentials.entities.builders.Text
import net.fabricmc.fabric.api.registry.CommandRegistry
import net.minecraft.server.command.ServerCommandSource
import java.util.function.Supplier
import io.github.nyliummc.essentials.api.EssentialsRegistry as APIEssentialsRegistry

object EssentialsRegistry : APIEssentialsRegistry {
    private val builders = mutableMapOf<Class<*>, Supplier<*>>()

    init {
        registerCommand(InfoCommand::register)

        builders[APICommand.Builder::class.java] = Supplier { return@Supplier Command.Builder() }
        builders[APITeleportRequest.Builder::class.java] = Supplier { return@Supplier TeleportRequest.Builder() }
        builders[APIText.Builder::class.java] = Supplier { return@Supplier Text.Builder() }
    }

    override fun registerCommand(callback: (CommandDispatcher<ServerCommandSource>) -> Unit) {
        CommandRegistry.INSTANCE.register(false, callback)
    }

    override fun registerCommand(callback: (APICommand,  CommandDispatcher<ServerCommandSource>) -> Unit) {
        CommandRegistry.INSTANCE.register(false) { dispatcher ->
            callback(Command, dispatcher)
        }
    }

    override fun <T> getBuilder(clz: Class<T>): T {
        return builders[clz]!!.get() as T
    }
}
