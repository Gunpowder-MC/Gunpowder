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

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import io.github.gunpowder.entities.DimensionManager;
import io.github.gunpowder.events.WorldPreSleepCallback;
import io.github.gunpowder.events.WorldSleepCallback;
import jdk.internal.vm.annotation.ForceInline;
import net.minecraft.SharedConstants;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.datafixer.Schemas;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resource.DataPackSettings;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.dynamic.RegistryOps;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.Spawner;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.SaveVersionInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
            CompoundTag tag = props.cloneWorldTag(getRegistryManager(), new CompoundTag());
            NbtIo.writeCompressed(tag, new File(server.session.getWorldDirectory(getRegistryKey()), "level.dat"));
        }
    }
}
