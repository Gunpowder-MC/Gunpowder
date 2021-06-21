/*
 * MIT License
 *
 * Copyright (c) GunpowderMC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.gunpowder.mixin.base;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import io.github.gunpowder.entities.arguments.ServerArgumentTypes;
import io.github.gunpowder.entities.builders.ArgumentType;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.Logger;
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
public class CommandManagerMixin_Base {
    @Redirect(at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;isDebugEnabled()Z", remap = false), method = "execute")
    private boolean isDebugEnabled(Logger logger) {
        return true;
    }

    /*
     * Source: https://gitlab.com/stellardrift/colonel/-/blob/dev/src/mixin/java/ca/stellardrift/colonel/mixin/MixinCommandManager.java
     */
    @Inject(method = "makeTreeForSource", at = @At(value = "INVOKE", target = "com.mojang.brigadier.builder.RequiredArgumentBuilder.getSuggestionsProvider()Lcom/mojang/brigadier/suggestion/SuggestionProvider;", remap = false, ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    public <T> void colonel$replaceArgumentType(CommandNode<ServerCommandSource> tree, CommandNode<CommandSource> result, ServerCommandSource source, Map<CommandNode<ServerCommandSource>, CommandNode<CommandSource>> resultNodes, CallbackInfo ci, Iterator<?> var5, CommandNode<ServerCommandSource> commandNode, ArgumentBuilder<?, ?> argumentBuilder, RequiredArgumentBuilder<?, T> requiredArgumentBuilder) throws CommandSyntaxException {
        ArgumentType type = ServerArgumentTypes.byClass((Class) requiredArgumentBuilder.getType().getClass());
        final Set<Identifier> knownExtraCommands = ServerArgumentTypes.getKnownArgumentTypes(source.getPlayer());

        while (type != null && !knownExtraCommands.contains(type.getId())) {
            ((RequiredArgumentBuilderAccessor_Base)requiredArgumentBuilder).accessor$type(type.getFallback().invoke(requiredArgumentBuilder.getType()));
            if (type.getSuggestions() != null) {
                requiredArgumentBuilder.suggests((SuggestionProvider) type.getSuggestions());
            }
            type = ServerArgumentTypes.byClass((Class) requiredArgumentBuilder.getType().getClass());
        }
    }
}
