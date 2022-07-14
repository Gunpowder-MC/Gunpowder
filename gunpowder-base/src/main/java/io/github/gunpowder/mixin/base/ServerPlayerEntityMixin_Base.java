package io.github.gunpowder.mixin.base;

import com.mojang.authlib.GameProfile;
import io.github.gunpowder.mixinterfaces.SyncPlayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.ExperienceBarUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin_Base extends PlayerEntity implements SyncPlayer {
    @Shadow
    public ServerPlayNetworkHandler networkHandler;

    private boolean sync;

    public ServerPlayerEntityMixin_Base(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Inject(method="teleport", at=@At(value="INVOKE", target="Lnet/minecraft/server/network/ServerPlayerEntity;setWorld(Lnet/minecraft/server/world/ServerWorld;)V"))
    void syncXP(ServerWorld targetWorld, double x, double y, double z, float yaw, float pitch, CallbackInfo ci) {
        networkHandler.sendPacket(new ExperienceBarUpdateS2CPacket(
                experienceProgress,
                totalExperience,
                experienceLevel
        ));
    }

    @Override
    public void setNeedsSync(boolean x) {
        sync = x;
    }

    @Override
    public boolean needsSync() {
        return sync;
    }
}

