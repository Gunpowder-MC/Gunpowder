package io.github.gunpowder.api.types

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.suggestion.SuggestionProvider
import net.minecraft.command.argument.serialize.ArgumentSerializer
import net.minecraft.command.suggestion.SuggestionProviders
import net.minecraft.util.Identifier

interface ServerArgumentType<T : ArgumentType<*>> {
    val id: Identifier
    val type: Class<out T>
    val serializer: ArgumentSerializer<T>
    val suggestions: SuggestionProvider<*>
        get() = SuggestionProviders.ASK_SERVER
    fun getFallback(argumentType: T): ArgumentType<*>
}
