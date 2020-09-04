package io.github.gunpowder.mixin.base;

import io.github.gunpowder.events.WorldPreSleepCallback;
import io.github.gunpowder.events.WorldSleepCallback;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin_Base {
    @Shadow @Final private List<ServerPlayerEntity> players;

    @Inject(method="tick", at=@At(value="INVOKE", target="Lnet/minecraft/server/world/ServerWorld;setTimeOfDay(J)V"))
    void triggerPreCallback(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        List<ServerPlayerEntity> players = this.players.stream().filter(LivingEntity::isSleeping).collect(Collectors.toList());
        WorldPreSleepCallback.EVENT.invoker().trigger((ServerWorld)(Object) this, players);
    }

    @Inject(method="wakeSleepingPlayers", at=@At("HEAD"))
    void triggerCallback(CallbackInfo ci) {
        List<ServerPlayerEntity> players = this.players.stream().filter(LivingEntity::isSleeping).collect(Collectors.toList());
        WorldSleepCallback.EVENT.invoker().trigger((ServerWorld)(Object) this, players);
    }
}
