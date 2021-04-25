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

import com.mojang.authlib.GameProfile;
import io.github.gunpowder.entities.LanguageHandler;
import io.github.gunpowder.events.PlayerDeathCallback;
import io.github.gunpowder.mixin.cast.SyncPlayer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientSettingsC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
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
    @Shadow public ServerPlayNetworkHandler networkHandler;

    private boolean sync;

    public ServerPlayerEntityMixin_Base(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Inject(method = "onDeath", at = @At("HEAD"))
    void triggerPlayerDeathCallback(DamageSource source, CallbackInfo ci) {
        PlayerDeathCallback.EVENT.invoker().trigger((ServerPlayerEntity) (Object) this, source);
    }

    @Inject(method="setClientSettings", at=@At("HEAD"))
    void storeLanguage(ClientSettingsC2SPacket packet, CallbackInfo ci) {
        LanguageHandler.INSTANCE.getLanguageSettings().put(this.uuid, packet.language);
    }

    @Inject(method="teleport", at=@At(value="INVOKE", target="Lnet/minecraft/server/network/ServerPlayerInteractionManager;setWorld(Lnet/minecraft/server/world/ServerWorld;)V"))
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
