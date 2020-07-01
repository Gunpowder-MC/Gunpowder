package io.github.nyliummc.essentials.mixin.datapacks;

import io.github.nyliummc.essentials.api.EssentialsMod;
import io.github.nyliummc.essentials.configs.DatapacksConfig;
import io.github.nyliummc.essentials.configs.VanillaTweaksConfig;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.NameTagItem;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(NameTagItem.class)
public class NameTagItemMixin_Datapacks_VT {
    @Inject(method="useOnEntity", at=@At("HEAD"), cancellable=true)
    void silenceMob(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (stack.hasCustomName() && !(entity instanceof PlayerEntity) && entity.isAlive()) {
            List<String> names = EssentialsMod.getInstance().getRegistry().getConfig(DatapacksConfig.class).getVanillaTweaks().getSilenceNametagValues();
            if (names.contains(stack.getName().asString())) {
                if (!user.world.isClient) {
                    entity.setSilent(true);
                    entity.setCustomName(new LiteralText("Silent"));
                    stack.decrement(1);
                }
                cir.setReturnValue(ActionResult.success(user.world.isClient));
            }
        }
    }
}
