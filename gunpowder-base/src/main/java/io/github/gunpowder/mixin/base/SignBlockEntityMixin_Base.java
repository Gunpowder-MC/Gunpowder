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

import io.github.gunpowder.entities.builders.SignType;
import io.github.gunpowder.mixin.cast.SignBlockEntityMixinCast_Base;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(SignBlockEntity.class)
public abstract class SignBlockEntityMixin_Base extends BlockEntity implements SignBlockEntityMixinCast_Base {
    boolean custom = false;
    SignType type = null;
    @Shadow
    @Final
    private Text[] text;
    @Shadow
    private PlayerEntity editor;
    public SignBlockEntityMixin_Base(BlockEntityType<?> type) {
        super(type);
    }

    @Shadow
    public abstract void setTextOnRow(int row, Text text);

    @Inject(method = "fromTag", at = @At("HEAD"))
    public void fromTag(BlockState state, CompoundTag tag, CallbackInfo ci) {
        type = (SignType) SignType.Companion.getRegistry().get(new Identifier(tag.getString("gunpowder:customType")));
        if (type != null) {
            type.getDeserializeEvent().invoke((SignBlockEntity) (Object) this, tag);
            custom = true;
        }
    }

    @Inject(method = "toTag", at = @At("HEAD"))
    public void toTag(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        if (type != null) {
            tag.putString("gunpowder:customType", io.github.gunpowder.entities.builders.SignType.Companion.getRegistry().getId(type).toString());
            type.getSerializeEvent().invoke((SignBlockEntity) (Object) this, tag);
        }
    }

    @Inject(method = "onActivate", at = @At("HEAD"), cancellable = true)
    void activate(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if (!player.world.isClient() && custom) {
            type.getClickEvent().invoke((SignBlockEntity) (Object) this, (ServerPlayerEntity) player);
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "setTextColor", at = @At("RETURN"))
    void keepHeaderColor(DyeColor value, CallbackInfoReturnable<Boolean> cir) {
        setTextOnRow(0, new LiteralText(text[0].asString()).styled((s) -> s.withColor(Formatting.BLUE)));
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (world == null || world.isClient()) return;

        String header = text[0].asString();
        if (header.startsWith("[") && header.endsWith("]")) {
            String signId = header.substring(1, header.length() - 1);
            Identifier[] ids = (Identifier[]) SignType.Companion.getRegistry().entriesById.keySet().stream().filter((id) -> id.getPath().equals(signId)).toArray();
            Optional<io.github.gunpowder.api.builders.SignType> typ = SignType.Companion.getRegistry().getOrEmpty(new Identifier(signId));

            if (ids.length > 1 && !typ.isPresent()) {
                // Multiple options, error
            }
            if (ids.length == 0 && !typ.isPresent()) {
                // Invalid name, do nothing
                return;
            }

            type = (SignType) typ.orElseGet(() -> SignType.Companion.getRegistry().get(ids[0]));

            if (type != null && type.getConditionEvent().invoke((SignBlockEntity) (Object) this, (ServerPlayerEntity) this.editor)) {
                setTextOnRow(0, new LiteralText(header).styled((s) -> s.withColor(Formatting.BLUE)));
                type.getCreateEvent().invoke((SignBlockEntity) (Object) this, (ServerPlayerEntity) this.editor);
                custom = true;
            }
        }
    }

    @Override
    public SignType getSignType() {
        return type;
    }

    @Override
    public boolean isCustom() {
        return custom;
    }
}
