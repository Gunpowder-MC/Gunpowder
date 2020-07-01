package io.github.nyliummc.essentials.mixin.datapacks;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.nyliummc.essentials.api.EssentialsMod;
import io.github.nyliummc.essentials.configs.CustomHead;
import io.github.nyliummc.essentials.configs.DatapacksConfig;
import io.github.nyliummc.essentials.configs.VanillaTweaksConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.predicate.NbtPredicate;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.apache.commons.lang3.text.WordUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Random;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin_Datapacks_VT extends Entity {
    public LivingEntityMixin_Datapacks_VT(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method="onDeath", at=@At(value="INVOKE", target="Lnet/minecraft/entity/LivingEntity;drop(Lnet/minecraft/entity/damage/DamageSource;)V"))
    void dropHead(DamageSource source, CallbackInfo ci) throws IOException {
        if (source.getAttacker() instanceof ServerPlayerEntity) {
            VanillaTweaksConfig cfg = EssentialsMod.getInstance().getRegistry().getConfig(DatapacksConfig.class).getVanillaTweaks();

            if (cfg.getMobHeadChance() != 0) {
                Identifier id = Registry.ENTITY_TYPE.getId(this.getType());
                boolean shouldDrop = new Random().nextInt(1000) < cfg.getMobHeadChance();

                List<CustomHead> heads = cfg.getCustomHeads();
                heads.removeIf((it) -> {
                    try {
                        if (!it.getId().equals(id.toString())) return false;
                        return new NbtPredicate(StringNbtReader.parse(it.getNbt())).test(this);
                    } catch (CommandSyntaxException var3) {
                        throw new JsonSyntaxException("Invalid nbt tag: " + var3.getMessage());
                    }
                });

                if (shouldDrop && heads.size() == 1) {
                    ItemStack stack;

                    switch(id.toString()) {
                        case "minecraft:skeleton":
                            stack = new ItemStack(Items.SKELETON_SKULL);
                            break;
                        case "minecraft:zombie":
                            stack = new ItemStack(Items.ZOMBIE_HEAD);
                            break;
                        case "minecraft:wither_skeleton":
                            stack = new ItemStack(Items.WITHER_SKELETON_SKULL);
                            break;
                        case "minecraft:creeper":
                            stack = new ItemStack(Items.CREEPER_HEAD);
                            break;
                        default:
                            stack = new ItemStack(Items.PLAYER_HEAD);
                            CompoundTag tag = new CompoundTag();
                            CompoundTag proptag = new CompoundTag();
                            ListTag texturestag = new ListTag();
                            String name = heads.get(0).getName();
                            String targetUrl = heads.get(0).getUrl();
                            String b64 = new String(Base64.getEncoder().encode(String.format("{\"textures\":{\"SKIN\":{\"url\":\"%s\"}}}", targetUrl).getBytes()));
                            NbtIo.read(new DataInputStream(new ByteArrayInputStream(String.format("{Name:\\\"%s\\\",Properties:{textures:[{Value:\\\"%s\\\"}]}}", name, b64).getBytes(StandardCharsets.UTF_8))));
                            proptag.put("textures", texturestag);
                            tag.put("Properties", proptag);
                            stack.getOrCreateTag().put("SkullOwner", tag);
                    }

                    ItemScatterer.spawn(world, getX(), getY(), getZ(), stack);
                }
            }
        }
    }
}
