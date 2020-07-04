package io.github.nyliummc.essentials.mixin.datapacks;

import io.github.nyliummc.essentials.api.EssentialsMod;
import io.github.nyliummc.essentials.configs.DatapacksConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.StreamSupport;

@Mixin(BeeEntity.class)
public class BeeEntityMixin_Datapacks_Voodoo {
    @Inject(method="tryAttack", at=@At("HEAD"), cancellable=true)
    void apiaristSuitCheck(Entity target, CallbackInfoReturnable<Boolean> cir) {
        if (EssentialsMod.getInstance().getRegistry().getConfig(DatapacksConfig.class).getVoodooBeard().getApiaristSuit()) {
            if (target instanceof PlayerEntity) {
                int chainmailItems = 0;
                Item[] items = new Item[] {Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS};
                for (ItemStack it : target.getArmorItems()) {
                    if (Arrays.stream(items).anyMatch((a) -> a == it.getItem())) {
                        chainmailItems += 1;
                    }
                }

                if (chainmailItems == 4) {
                    cir.setReturnValue(false);
                }
            }
        }
    }
}
