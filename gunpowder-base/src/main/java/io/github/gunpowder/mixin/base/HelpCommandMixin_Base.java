package io.github.gunpowder.mixin.base;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.HelpCommand;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HelpCommand.class)
public abstract class HelpCommandMixin_Base {
    @Inject(method = "register", at=@At("HEAD"), cancellable = true)
    private static void noHelp(CommandDispatcher<ServerCommandSource> dispatcher, CallbackInfo ci) {
        ci.cancel();
    }
}
