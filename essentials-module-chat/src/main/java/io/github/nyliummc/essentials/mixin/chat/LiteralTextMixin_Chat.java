package io.github.nyliummc.essentials.mixin.chat;

import io.github.nyliummc.essentials.api.EssentialsMod;
import io.github.nyliummc.essentials.configs.ChatConfig;
import io.github.nyliummc.essentials.entities.TextFormatter;
import net.minecraft.text.BaseText;
import net.minecraft.text.LiteralText;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LiteralText.class)
public abstract class LiteralTextMixin_Chat extends BaseText {
    @Mutable
    @Final
    @Shadow
    private String string;


    @Inject(method="<init>", at = @At("RETURN"))
    public void LiteralText(String string, CallbackInfo ci) {
        try {
            if (EssentialsMod.getInstance().getRegistry().getConfig(ChatConfig.class).getEnableChatColors()) {
                this.string = TextFormatter.INSTANCE.formatString(string);
            } else {
                this.string = string;
            }
        } catch (NullPointerException npe) {

        }
    }
}
