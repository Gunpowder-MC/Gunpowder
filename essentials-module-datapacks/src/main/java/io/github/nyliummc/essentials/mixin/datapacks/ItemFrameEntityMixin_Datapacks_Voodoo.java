package io.github.nyliummc.essentials.mixin.datapacks;

import io.github.nyliummc.essentials.api.EssentialsMod;
import io.github.nyliummc.essentials.configs.DatapacksConfig;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemFrameEntity.class)
public abstract class ItemFrameEntityMixin_Datapacks_Voodoo extends AbstractDecorationEntity {
    @Shadow public abstract void setHeldItemStack(ItemStack stack);

    protected ItemFrameEntityMixin_Datapacks_Voodoo(EntityType<? extends AbstractDecorationEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method= "setHeldItemStack(Lnet/minecraft/item/ItemStack;)V", at=@At("HEAD"), cancellable=true)
    void makeInvisible(ItemStack stack, CallbackInfo ci) {
        if (stack.getItem() == Items.POTION && !isInvisible()) {
            if (EssentialsMod.getInstance().getRegistry().getConfig(DatapacksConfig.class).getVoodooBeard().getInvisibleItemFrames() && (PotionUtil.getPotion(stack) == Potions.INVISIBILITY || PotionUtil.getPotion(stack) == Potions.LONG_INVISIBILITY)) {
                setInvisible(true);
                setHeldItemStack(new ItemStack(Items.GLASS_BOTTLE));
                ci.cancel();
            }
        }
    }
}
