package io.github.nyliummc.essentials.mixin.dynmap;

import io.github.nyliummc.essentials.EssentialsDynmapModule;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import org.dynmap.common.DynmapListenerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class BlockMixin_Dynmap {
    @Inject(method="onBroken", at=@At("RETURN"))
    void event(IWorld world, BlockPos pos, BlockState state, CallbackInfo ci) {
        EssentialsDynmapModule.core.listenerManager.processBlockEvent(DynmapListenerManager.EventType.BLOCK_BREAK, ((Block)(Object)this).getMaterial(state).toString(), world.toString(), pos.getX(), pos.getY(), pos.getZ());
    }
}
