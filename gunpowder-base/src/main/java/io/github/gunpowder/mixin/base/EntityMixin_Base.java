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

import io.github.gunpowder.api.GunpowderMod;
import io.github.gunpowder.api.components.ComponentsKt;
import io.github.gunpowder.entities.ComponentHandler;
import io.github.gunpowder.entities.DimensionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin_Base {
    @Inject(method="<init>", at=@At("RETURN"))
    void initComponents(EntityType<?> type, World world, CallbackInfo ci) {
        ComponentHandler.INSTANCE.initComponents(this);
    }

    @Inject(method="writeNbt", at=@At("HEAD"))
    void saveComponents(NbtCompound tag, CallbackInfoReturnable<NbtCompound> cir) {
        ComponentHandler.INSTANCE.saveComponents(tag, this);
    }

    @Inject(method="readNbt", at=@At("HEAD"))
    void loadComponents(NbtCompound tag, CallbackInfo ci) {
        ComponentHandler.INSTANCE.loadComponents(tag, this);
    }

    @Inject(method="tick", at=@At("HEAD"))
    void tickComponents(CallbackInfo ci) {
        ComponentHandler.INSTANCE.tickComponents(this);
    }

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
        if (world.getRegistryKey() != World.END && DimensionManager.INSTANCE.getNetherMap().get(world.getRegistryKey()) == null) {
            // To generate a nether portal, we need to return World.NETHER
            return World.NETHER;
        }
        return world.getRegistryKey();
    }

    @Redirect(method = "getTeleportTarget", at=@At(value="INVOKE", target="Lnet/minecraft/server/world/ServerWorld;getRegistryKey()Lnet/minecraft/util/registry/RegistryKey;"))
    RegistryKey<World> goingToNether(ServerWorld world) {
        if (world.getRegistryKey() != World.END && DimensionManager.INSTANCE.getNetherMap().get(world.getRegistryKey()) == null) {
            return World.NETHER;
        }
        return world.getRegistryKey();
    }
}
