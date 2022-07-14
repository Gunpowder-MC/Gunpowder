package io.github.gunpowder.mixin.base;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import io.github.gunpowder.api.types.ServerArgumentType;
import io.github.gunpowder.mixinterfaces.ArgumentTypeSetter;
import io.github.gunpowder.mod.GunpowderRegistryImpl;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Mixin(CommandManager.class)
public abstract class CommandManagerMixin_Base {
    @Redirect(at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;isDebugEnabled()Z", remap = false), method = "execute")
    private boolean isDebugEnabled(Logger logger) {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "makeTreeForSource", at = @At(value = "INVOKE", target = "com.mojang.brigadier.builder.RequiredArgumentBuilder.getSuggestionsProvider()Lcom/mojang/brigadier/suggestion/SuggestionProvider;", remap = false, ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    public <S, T> void replaceArgumentType(CommandNode<ServerCommandSource> tree, CommandNode<CommandSource> result, ServerCommandSource source, Map<CommandNode<ServerCommandSource>, CommandNode<CommandSource>> resultNodes, CallbackInfo ci, Iterator<?> var5, CommandNode<ServerCommandSource> commandNode, ArgumentBuilder<?, ?> argumentBuilder, RequiredArgumentBuilder<S, T> requiredArgumentBuilder) throws CommandSyntaxException {
        ServerArgumentType<ArgumentType<T>> type = GunpowderRegistryImpl.INSTANCE.argumentTypeByClass((Class<ArgumentType<T>>) requiredArgumentBuilder.getType().getClass());
        final Set<Identifier> knownExtraCommands = GunpowderRegistryImpl.INSTANCE.getKnownArgumentTypes(source.getPlayer());

        while (type != null && !knownExtraCommands.contains(type.getId())) {
            ((ArgumentTypeSetter)requiredArgumentBuilder).setType(type.getFallback(requiredArgumentBuilder.getType()));
            requiredArgumentBuilder.suggests((SuggestionProvider<S>) type.getSuggestions());
            type = GunpowderRegistryImpl.INSTANCE.argumentTypeByClass((Class<ArgumentType<T>>) requiredArgumentBuilder.getType().getClass());
        }
    }
}

