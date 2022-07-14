package io.github.gunpowder.mixin.base;

import io.github.gunpowder.api.types.SignType;
import io.github.gunpowder.mod.GunpowderRegistryImpl;
import io.github.gunpowder.mixinterfaces.SignTypeBE;
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
import net.minecraft.util.registry.SimpleRegistry;
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
public abstract class SignBlockEntityMixin_Base extends BlockEntity implements SignTypeBE {
    public SignBlockEntityMixin_Base(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Shadow
    public abstract void setTextOnRow(int row, Text text);

    @Shadow @Final
    private Text[] texts;

    @Shadow @Nullable
    private UUID editor;

    private SignType gpSignType;

    @Inject(method = "onActivate", at = @At("HEAD"), cancellable = true)
    void activate(ServerPlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if (gpSignType != null) {
            gpSignType.onClick((SignBlockEntity)(Object)this, player);
        }
    }

    @Inject(method = "setTextColor", at = @At("RETURN"))
    void keepHeaderColor(DyeColor value, CallbackInfoReturnable<Boolean> cir) {
        if (gpSignType != null) {
            setTextOnRow(0, new LiteralText(texts[0].asString()).styled(s -> s.withColor(Formatting.BLUE)));
        }
    }

    @Inject(method="readNbt", at=@At("HEAD"))
    void readSignData(NbtCompound nbt, CallbackInfo ci) {
        NbtCompound tag = nbt.getCompound("gp:signdata");
        if (tag != null) {
            String id = tag.getString("gp:signtypeid");
            SimpleRegistry<SignType> reg = GunpowderRegistryImpl.INSTANCE.getSignRegistry();
            Identifier[] ids = reg.idToEntry.keySet().stream().filter(j -> j.getPath().equals(id)).toArray(Identifier[]::new);
            Optional<SignType> typ = reg.getOrEmpty(new Identifier(id));
            SignType type = typ.orElseGet(() -> reg.get(ids[0]));
            if (type != null) {
                type.deserialize((SignBlockEntity) (Object) this, tag);
            }
            gpSignType = type;
        }
    }

    @Inject(method="writeNbt", at=@At("HEAD"))
    void writeSignData(NbtCompound nbt, CallbackInfo ci) {
        if (gpSignType != null) {
            NbtCompound tag = new NbtCompound();
            gpSignType.serialize((SignBlockEntity) (Object) this, tag);
            tag.putString("gp:signtypeid", gpSignType.getId().toString());
            nbt.put("gp:signdata", tag);
        }
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (world == null || world.isClient()) return;

        String header = texts[0].asString();
        if (header.startsWith("[") && header.endsWith("]")) {
            SimpleRegistry<SignType> reg = GunpowderRegistryImpl.INSTANCE.getSignRegistry();
            String signId = header.substring(1, header.length() - 1);
            Identifier[] ids = reg.idToEntry.keySet().stream().filter(id -> id.getPath().equals(signId)).toArray(Identifier[]::new);
            Optional<SignType> typ = reg.getOrEmpty(new Identifier(signId));
            ServerPlayerEntity player = GunpowderRegistryImpl.INSTANCE.getServer().getPlayerManager().getPlayer(editor);

            if (player == null) {
                return;
            }

            if (ids.length > 1 && typ.isEmpty()) {
                // Multiple options, error
                player.sendMessage(new LiteralText("Multiple signs with this ID, please be more specific."), false);
                return;
            }
            if (ids.length == 0 && typ.isEmpty()) {
                // Invalid name, do nothing
                return;
            }

            SignType type = typ.orElseGet(() -> reg.get(ids[0]));

            if (type != null && type.canCreate((SignBlockEntity) (Object) this, player)) {
                setTextOnRow(0, new LiteralText(header).styled(s -> s.withColor(Formatting.BLUE)));
                type.onCreate((SignBlockEntity) (Object) this, player);
                this.gpSignType = type;
            }
        }
    }

    @Override
    public boolean isCustom() {
        return gpSignType != null;
    }

    @Override
    public boolean isCreator(@Nullable PlayerEntity player) {
        return player != null && editor != null && editor.equals(player.getUuid());
    }

    @Nullable
    @Override
    public SignType getSignType() {
        return gpSignType;
    }
}
