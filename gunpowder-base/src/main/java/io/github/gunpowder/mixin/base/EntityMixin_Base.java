package io.github.gunpowder.mixin.base;

import io.github.gunpowder.api.GunpowderMod;
import io.github.gunpowder.entities.DimensionManager;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public class EntityMixin_Base {
    @Redirect(method="tickNetherPortal", at=@At(value="INVOKE", target="Lnet/minecraft/server/MinecraftServer;getWorld(Lnet/minecraft/util/registry/RegistryKey;)Lnet/minecraft/server/world/ServerWorld;"))
    ServerWorld lookupMap(MinecraftServer minecraftServer, RegistryKey<World> key) {
        RegistryKey<World> realKey = ((Entity)(Object)this).world.getRegistryKey();
        RegistryKey<World> target = DimensionManager.INSTANCE.getNetherMap().get(realKey);
        if (target == null) {
            target = DimensionManager.INSTANCE.getNetherMap().inverse().get(realKey);
        }
        return GunpowderMod.getInstance().getServer().getWorld(target);
    }

    @Redirect(method = "getTeleportTarget", at=@At(value="INVOKE", target="Lnet/minecraft/world/World;getRegistryKey()Lnet/minecraft/util/registry/RegistryKey;"))
    RegistryKey<World> comingFromNether(World world) {
        if (DimensionManager.INSTANCE.getNetherMap().get(world.getRegistryKey()) == null) {
            // To generate a nether portal, we need to return World.NETHER
            return World.NETHER;
        }
        return world.getRegistryKey();
    }

    @Redirect(method = "getTeleportTarget", at=@At(value="INVOKE", target="Lnet/minecraft/server/world/ServerWorld;getRegistryKey()Lnet/minecraft/util/registry/RegistryKey;"))
    RegistryKey<World> goingToNether(ServerWorld world) {
        if (DimensionManager.INSTANCE.getNetherMap().get(world.getRegistryKey()) == null) {
            return World.NETHER;
        }
        return world.getRegistryKey();
    }
}
