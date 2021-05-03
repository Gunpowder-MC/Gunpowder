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

import io.github.gunpowder.events.PlayerPreDeathCallback;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static org.checkerframework.checker.units.UnitsTools.g;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin_Base extends Entity {
    public LivingEntityMixin_Base(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow protected abstract void playHurtSound(DamageSource source);

    @Shadow private DamageSource lastDamageSource;

    @Shadow private long lastDamageTime;

    @Inject(method="damage", at=@At(value="INVOKE", target="Lnet/minecraft/entity/LivingEntity;isDead()Z", ordinal=1, shift=At.Shift.BEFORE), cancellable=true, locals= LocalCapture.CAPTURE_FAILHARD)
    void beforeFAPIEvent(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir, float f, boolean bl, float g, boolean bl2, Entity entity2) {
        if (((LivingEntity)(Object)this) instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
            ActionResult res = PlayerPreDeathCallback.EVENT.invoker().trigger(player, source);

            if (res == ActionResult.FAIL) {
                if (bl2) {
                    this.playHurtSound(source);
                }

                boolean bl3 = !bl || amount > 0.0F;
                if (bl3) {
                    this.lastDamageSource = source;
                    this.lastDamageTime = this.world.getTime();
                }

                Criteria.ENTITY_HURT_PLAYER.trigger(player, source, f, amount, bl);
                if (g > 0.0F && g < 3.4028235E37F) {
                    player.increaseStat(Stats.DAMAGE_BLOCKED_BY_SHIELD, Math.round(g * 10.0F));
                }

                if (entity2 instanceof ServerPlayerEntity) {
                    Criteria.PLAYER_HURT_ENTITY.trigger((ServerPlayerEntity)entity2, player, source, f, amount, bl);
                }

                cir.setReturnValue(bl3);
            }
        }
    }
}
