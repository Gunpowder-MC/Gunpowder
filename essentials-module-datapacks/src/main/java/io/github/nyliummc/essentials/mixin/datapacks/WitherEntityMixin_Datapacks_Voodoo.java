package io.github.nyliummc.essentials.mixin.datapacks;

import io.github.nyliummc.essentials.api.EssentialsMod;
import io.github.nyliummc.essentials.configs.DatapacksConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WitherEntity.class)
public class WitherEntityMixin_Datapacks_Voodoo extends HostileEntity {
    protected WitherEntityMixin_Datapacks_Voodoo(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Redirect(method="mobTick", at=@At(value="INVOKE", target="Lnet/minecraft/entity/Entity;isSilent()Z"))
    public boolean disableGlobalSound(Entity entity) {
        return EssentialsMod.getInstance().getRegistry().getConfig(DatapacksConfig.class).getVoodooBeard().getSilentWither() || entity.isSilent();
    }
}
