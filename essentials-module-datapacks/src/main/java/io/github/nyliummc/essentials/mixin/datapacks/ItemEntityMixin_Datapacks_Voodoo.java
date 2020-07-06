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
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.SaplingBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin_Datapacks_Voodoo extends Entity {
    public ItemEntityMixin_Datapacks_Voodoo(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow public abstract ItemStack getStack();

    @Inject(method="tick", at=@At("HEAD"), cancellable=true)
    void plantSaplings(CallbackInfo ci) {
        Item i = getStack().getItem();
        if (i instanceof BlockItem && age >= 600) {
            Block b = ((BlockItem) i).getBlock();
            if (b instanceof SaplingBlock && EssentialsMod.getInstance().getRegistry().getConfig(DatapacksConfig.class).getVoodooBeard().getAutoSaplings()) {
                if (world.getBlockState(getBlockPos().down()).getMaterial() == Material.SOIL) {
                    remove();
                    world.setBlockState(getBlockPos(), b.getDefaultState());
                }
            }
        }
    }
}

