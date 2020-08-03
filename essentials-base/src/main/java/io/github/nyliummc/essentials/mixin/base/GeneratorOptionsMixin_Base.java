package io.github.nyliummc.essentials.mixin.base;

import com.mojang.serialization.Lifecycle;
import io.github.nyliummc.essentials.entities.mc.WrapperRegistry;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GeneratorOptions.class)
public class GeneratorOptionsMixin_Base {
    @Shadow @Final public SimpleRegistry<DimensionOptions> field_24827;

    @Inject(method="getDimensionMap", at=@At("HEAD"), cancellable = true)
    void customMapWrapper(CallbackInfoReturnable<SimpleRegistry<DimensionOptions>> cir) {
        cir.setReturnValue(new WrapperRegistry<>(Registry.DIMENSION_OPTIONS, Lifecycle.experimental(), field_24827, new DimensionOptions(() -> null, new FlatChunkGenerator(FlatChunkGeneratorConfig.getDefaultConfig()))));
    }
}
