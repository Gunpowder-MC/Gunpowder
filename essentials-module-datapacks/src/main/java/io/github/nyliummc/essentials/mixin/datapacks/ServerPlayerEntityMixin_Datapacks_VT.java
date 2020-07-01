package io.github.nyliummc.essentials.mixin.datapacks;

import com.mojang.authlib.GameProfile;
import io.github.nyliummc.essentials.api.EssentialsMod;
import io.github.nyliummc.essentials.configs.DatapacksConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.function.UnaryOperator;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin_Datapacks_VT extends PlayerEntity {
    public ServerPlayerEntityMixin_Datapacks_VT(World world, BlockPos blockPos, GameProfile gameProfile) {
        super(world, blockPos, gameProfile);
    }

    @Inject(method="onDeath", at=@At(value="INVOKE", target="Lnet/minecraft/entity/LivingEntity;drop(Lnet/minecraft/entity/damage/DamageSource;)V"))
    void dropHead(DamageSource source, CallbackInfo ci) {
        if (source.getAttacker() instanceof ServerPlayerEntity && source.getAttacker() != this) {
            // Killed by another player
            if (EssentialsMod.getInstance().getRegistry().getConfig(DatapacksConfig.class).getVanillaTweaks().getPlayersDropHeads()) {
                // Dropping heads enabled
                ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
                Text name = this.getCustomName();
                if (name == null) {
                    name = this.getName();
                }
                stack.setCustomName(new LiteralText(name.asString() + "'s Head").styled(style -> style.withColor(Formatting.DARK_BLUE)));
                stack.getOrCreateTag().put("SkullOwner", NbtHelper.fromGameProfile(new CompoundTag(), this.getGameProfile()));
                ItemScatterer.spawn(this.world, this.getX(), this.getY(), this.getZ(), stack);
            }
        }
    }
}
