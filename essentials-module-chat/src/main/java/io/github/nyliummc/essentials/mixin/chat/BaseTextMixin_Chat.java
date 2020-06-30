package io.github.nyliummc.essentials.mixin.chat;

import net.minecraft.text.BaseText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BaseText.class)
public abstract class BaseTextMixin_Chat {
    // Mumfrey @ Sponge Discord:
    // https://discord.com/channels/142425412096491520/626802111455297538/727499709824106498
    @Shadow public abstract Style getStyle();
}
