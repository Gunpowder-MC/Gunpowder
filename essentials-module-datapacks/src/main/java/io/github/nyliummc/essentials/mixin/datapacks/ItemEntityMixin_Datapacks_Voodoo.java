package io.github.nyliummc.essentials.mixin.datapacks;

import io.github.nyliummc.essentials.api.EssentialsMod;
import io.github.nyliummc.essentials.configs.DatapacksConfig;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.SaplingBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin_Datapacks_Voodoo extends Entity {
    public ItemEntityMixin_Datapacks_Voodoo(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow public abstract ItemStack getStack();

    @Inject(method="tick", at=@At("HEAD"), cancellable=true)
    void plantSaplings(CallbackInfo ci) {
        Item i = getStack().getItem();
        if (i instanceof BlockItem && age >= 600) {
            Block b = ((BlockItem) i).getBlock();
            if (b instanceof SaplingBlock && EssentialsMod.getInstance().getRegistry().getConfig(DatapacksConfig.class).getVoodooBeard().getAutoSaplings()) {
                if (world.getBlockState(getBlockPos().down()).getMaterial() == Material.SOIL) {
                    remove();
                    world.setBlockState(getBlockPos(), b.getDefaultState());
                }
            }
        }
    }
}

