package io.github.nyliummc.essentials.mixin.chat;

import net.minecraft.text.BaseText;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TranslatableText.class)
public abstract class TranslatableTextMixin_Chat extends BaseText {
    @Mutable
    @Final
    @Shadow
    private String key;
    @Mutable
    @Final
    @Shadow
    private Object[] args;

    protected TranslatableTextMixin_Chat(String key, Object[] args) {
        this.key = key;
        this.args = args;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void TranslatableText(String key, Object[] args, CallbackInfo ci) {
        this.key = key;
        this.args = args;

        for(int i = 0; i < args.length; ++i) {
            Object object = args[i];
            if(object instanceof String){
                this.args[i] = new LiteralText((String) object);
            }
            else if (object instanceof Text) {
                Text text = ((Text)object).deepCopy();
                this.args[i] = text;
                text.getStyle().setParent(this.getStyle());
            } else if (object == null) {
                this.args[i] = "null";
            }
        }
    }
}
