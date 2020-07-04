package io.github.nyliummc.essentials.mixin.datapacks;

import io.github.nyliummc.essentials.api.EssentialsMod;
import io.github.nyliummc.essentials.configs.DatapacksConfig;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.EndermiteEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EndermiteEntity.class)
public class EndermiteEntityMixin_Datapacks_Voodoo extends HostileEntity {
    @Shadow private int lifeTime;

    protected EndermiteEntityMixin_Datapacks_Voodoo(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method="tick", at=@At("HEAD"), cancellable=true)
    void burrow(CallbackInfo ci) {
        if (EssentialsMod.getInstance().getRegistry().getConfig(DatapacksConfig.class).getVoodooBeard().getShulkermites()) {
            BlockPos below = getBlockPos().down();
            if (this.lifeTime >= 2380 && world.getBlockState(below).getBlock() == Blocks.PURPUR_BLOCK) {
                world.setBlockState(below, Blocks.AIR.getDefaultState());
                remove();
                ShulkerEntity e = EntityType.SHULKER.create(world);
                e.refreshPositionAndAngles(below, 0, 0);
                CompoundTag t = new CompoundTag();
                t.putByte("Color", (byte) 16);
                e.readCustomDataFromTag(t);
                e.initialize(world, world.getLocalDifficulty(below), SpawnReason.MOB_SUMMONED, null, null);
                world.spawnEntity(e);
            }
        }
    }
}
