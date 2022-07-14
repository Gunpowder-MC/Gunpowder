package io.github.gunpowder.mixin.base;

import io.github.gunpowder.mixinterfaces.SignTypeBE;
import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractSignBlock.class)
public abstract class SignBlockMixin_Base extends Block {
    public SignBlockMixin_Base(Settings settings) {
        super(settings);
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        SignBlockEntity be = (SignBlockEntity) world.getBlockEntity(pos);
        SignTypeBE signTypeBE = (SignTypeBE) be;
        if (!world.isClient() && be != null && signTypeBE.getSignType() != null) {
            signTypeBE.getSignType().onDestroy(be, (ServerPlayerEntity) player);
        }
        super.onBreak(world, pos, state, player);
    }
}
