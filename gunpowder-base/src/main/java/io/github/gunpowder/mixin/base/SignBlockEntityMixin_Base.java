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
import io.github.gunpowder.entities.builders.SignType;
import io.github.gunpowder.mixin.cast.SignBlockEntityMixinCast_Base;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.UUID;

@Mixin(SignBlockEntity.class)
public abstract class SignBlockEntityMixin_Base extends BlockEntity implements SignBlockEntityMixinCast_Base {
    boolean custom = false;
    UUID owner;
    SignType type = null;

    public SignBlockEntityMixin_Base(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Shadow
    public abstract void setTextOnRow(int row, Text text);

    @Shadow @Final private Text[] texts;

    @Shadow @Nullable private UUID editor;

    @Inject(method = "readNbt", at = @At("HEAD"))
    public void fromTag(NbtCompound tag, CallbackInfo ci) {
        type = (SignType) SignType.Companion.getRegistry().get(new Identifier(tag.getString("gunpowder:customType")));
        if (type != null) {
            type.getDeserializeEvent().invoke((SignBlockEntity) (Object) this, tag);
            custom = true;
            owner = tag.getUuid("gunpowder:owner");
        }
    }

    @Inject(method = "writeNbt", at = @At("HEAD"))
    public void toTag(NbtCompound tag, CallbackInfoReturnable<NbtCompound> cir) {
        if (type != null) {
            tag.putString("gunpowder:customType", io.github.gunpowder.entities.builders.SignType.Companion.getRegistry().getId(type).toString());
            tag.putUuid("gunpowder:owner", owner);
            type.getSerializeEvent().invoke((SignBlockEntity) (Object) this, tag);
        }
    }

    @Inject(method = "onActivate", at = @At("HEAD"), cancellable = true)
    void activate(ServerPlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if (!player.world.isClient() && custom) {
            type.getClickEvent().invoke((SignBlockEntity) (Object) this, player);
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "setTextColor", at = @At("RETURN"))
    void keepHeaderColor(DyeColor value, CallbackInfoReturnable<Boolean> cir) {
        if (custom) {
            setTextOnRow(0, new LiteralText(texts[0].asString()).styled((s) -> s.withColor(Formatting.BLUE)));
        }
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (world == null || world.isClient()) return;

        String header = texts[0].asString();
        if (header.startsWith("[") && header.endsWith("]")) {
            String signId = header.substring(1, header.length() - 1);
            Identifier[] ids = SignType.Companion.getRegistry().idToEntry.keySet().stream().filter((id) -> id.getPath().equals(signId)).toArray(Identifier[]::new);
            Optional<io.github.gunpowder.api.builders.SignType> typ = SignType.Companion.getRegistry().getOrEmpty(new Identifier(signId));
            ServerPlayerEntity player = GunpowderMod.getInstance().getServer().getPlayerManager().getPlayer(editor);

            if (ids.length > 1 && typ.isEmpty()) {
                // Multiple options, error
                player.sendMessage(new LiteralText("Multiple signs with this ID, please be more specific."), false);
                return;
            }
            if (ids.length == 0 && typ.isEmpty()) {
                // Invalid name, do nothing
                return;
            }

            type = (SignType) typ.orElseGet(() -> SignType.Companion.getRegistry().get(ids[0]));

            if (type != null && type.getConditionEvent().invoke((SignBlockEntity) (Object) this, player)) {
                setTextOnRow(0, new LiteralText(header).styled((s) -> s.withColor(Formatting.BLUE)));
                type.getCreateEvent().invoke((SignBlockEntity) (Object) this, player);
                custom = true;
                owner = editor;
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

    @Override
    public boolean isCreator(PlayerEntity playerEntity) {
        return playerEntity.getUuid().equals(owner);
    }
}
