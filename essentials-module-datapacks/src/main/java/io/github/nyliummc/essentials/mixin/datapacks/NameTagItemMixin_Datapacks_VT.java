/*
 * MIT License
 *
 * Copyright (c) NyliumMC
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

package io.github.nyliummc.essentials.mixin.datapacks;

import io.github.nyliummc.essentials.api.EssentialsMod;
import io.github.nyliummc.essentials.configs.DatapacksConfig;
import io.github.nyliummc.essentials.configs.VanillaTweaksConfig;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.NameTagItem;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(NameTagItem.class)
public class NameTagItemMixin_Datapacks_VT {
    @Inject(method="useOnEntity", at=@At("HEAD"), cancellable=true)
    void silenceMob(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (stack.hasCustomName() && !(entity instanceof PlayerEntity) && entity.isAlive()) {
            List<String> names = EssentialsMod.getInstance().getRegistry().getConfig(DatapacksConfig.class).getVanillaTweaks().getSilenceNametagValues();
            if (names.contains(stack.getName().asString())) {
                if (!user.world.isClient) {
                    entity.setSilent(true);
                    entity.setCustomName(new LiteralText("Silent"));
                    stack.decrement(1);
                }
                cir.setReturnValue(ActionResult.success(user.world.isClient));
            }
        }
    }
}
