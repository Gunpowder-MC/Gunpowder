/*
 * MIT License
 *
 * Copyright (c) GunpowderMC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.gunpowder.mixin.base;

import io.github.gunpowder.entities.ComponentHandler;
import io.github.gunpowder.entities.DimensionManager;
import io.github.gunpowder.entities.mc.ComponentState;
import io.github.gunpowder.events.WorldPreSleepCallback;
import io.github.gunpowder.events.WorldSleepCallback;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.Spawner;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin_Base extends World {
    @Shadow @Final private List<ServerPlayerEntity> players;

    @Mutable
    @Shadow @Final private ServerWorldProperties worldProperties;

    @Shadow public abstract DynamicRegistryManager getRegistryManager();

    @Shadow @Final private MinecraftServer server;

    @Shadow public abstract PersistentStateManager getPersistentStateManager();

    protected ServerWorldMixin_Base(MutableWorldProperties properties, RegistryKey<World> registryRef, DimensionType dimensionType, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed) {
        super(properties, registryRef, dimensionType, profiler, isClient, debugWorld, seed);
    }

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

    @Inject(method="save", at=@At(value="INVOKE", target="Lnet/minecraft/server/world/ServerWorld;saveLevel()V"))
    void saveProperties(ProgressListener progressListener, boolean flush, boolean bl, CallbackInfo ci) throws IOException {
        if (DimensionManager.INSTANCE.isCustom(getRegistryKey())) {
            LevelProperties props = ((LevelProperties)getLevelProperties());
            NbtCompound tag = props.cloneWorldNbt(getRegistryManager(), new NbtCompound());
            NbtIo.writeCompressed(tag, new File(server.session.getWorldDirectory(getRegistryKey()), "level.dat"));
        }
    }

    @Inject(method="<init>", at=@At("RETURN"))
    void initComponents(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session, ServerWorldProperties properties, RegistryKey<World> registryKey, DimensionType dimensionType, WorldGenerationProgressListener worldGenerationProgressListener, ChunkGenerator chunkGenerator, boolean debugWorld, long l, List<Spawner> list, boolean bl, CallbackInfo ci) {
        ComponentHandler.INSTANCE.initComponents(this);

        getPersistentStateManager().getOrCreate(
                (nbt) -> ComponentState.fromNbt(nbt, (ServerWorld)(Object) this),
                () -> new ComponentState((ServerWorld) (Object) this),
                "gpcomponents"
        );
    }
}
