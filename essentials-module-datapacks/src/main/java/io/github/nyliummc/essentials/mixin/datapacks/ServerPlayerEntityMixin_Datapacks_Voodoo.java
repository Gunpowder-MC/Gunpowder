package io.github.nyliummc.essentials.mixin.datapacks;

import com.mojang.authlib.GameProfile;
import io.github.nyliummc.essentials.api.EssentialsMod;
import io.github.nyliummc.essentials.configs.DatapacksConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Arrays;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin_Datapacks_Voodoo extends PlayerEntity {
    public ServerPlayerEntityMixin_Datapacks_Voodoo(World world, BlockPos blockPos, GameProfile gameProfile) {
        super(world, blockPos, gameProfile);
    }

    @Override
    public boolean isFireImmune() {
        if (EssentialsMod.getInstance().getRegistry().getConfig(DatapacksConfig.class).getVoodooBeard().getNetheriteFireImmune()) {
            int netheriteItems = 0;
            Item[] items = new Item[] {Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS};
            for (ItemStack it : getArmorItems()) {
                if (Arrays.stream(items).anyMatch((a) -> a == it.getItem())) {
                    netheriteItems += 1;
                }
            }

            if (netheriteItems == 4) {
                return false;
            }
        }
        return super.isFireImmune();
    }
}
