package io.github.nyliummc.essentials.mixin.base;

import com.mojang.authlib.GameProfile;
import io.github.nyliummc.essentials.events.PlayerDeathCallback;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin_Base extends PlayerEntity {
    public ServerPlayerEntityMixin_Base(World world, BlockPos blockPos, GameProfile gameProfile) {
        super(world, blockPos, gameProfile);
    }

    @Inject(method="onDeath", at=@At("HEAD"))
    void triggerPlayerDeathCallback(DamageSource source, CallbackInfo ci) {
        PlayerDeathCallback.EVENT.invoker().trigger((ServerPlayerEntity) (Object)this, source);
    }
}
