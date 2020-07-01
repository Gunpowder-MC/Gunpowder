package io.github.nyliummc.essentials.mixin.datapacks;

import io.github.nyliummc.essentials.api.EssentialsMod;
import io.github.nyliummc.essentials.configs.DatapacksConfig;
import io.github.nyliummc.essentials.configs.VanillaTweaksConfig;
import net.minecraft.block.Block;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.tag.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EndermanEntity.PickUpBlockGoal.class)
public class EndermanEntityMixin_Datapacks_VT {
    @Redirect(method="tick", at=@At(value="INVOKE_ASSIGN", target="Lnet/minecraft/block/Block;isIn(Lnet/minecraft/tag/Tag;)Z"))
    boolean stopPickup(Block block, Tag<Block> tag) {
        return block.isIn(tag) && EssentialsMod.getInstance().getRegistry().getConfig(DatapacksConfig.class).getVanillaTweaks().getAllowEndermanPickup();
    }
}
