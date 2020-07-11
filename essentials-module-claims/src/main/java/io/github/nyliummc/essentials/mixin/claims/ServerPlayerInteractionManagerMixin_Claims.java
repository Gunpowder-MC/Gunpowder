package io.github.nyliummc.essentials.mixin.claims;

import io.github.nyliummc.essentials.api.EssentialsMod;
import io.github.nyliummc.essentials.api.module.claims.modelhandlers.ClaimHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin_Claims {
    @Inject(method="interactBlock", at=@At(value="INVOKE",target="Lnet/minecraft/block/BlockState;onUse(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;"), cancellable = true)
    void preventInteraction(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        ChunkPos chunk = new ChunkPos(hitResult.getBlockPos());
        ClaimHandler handler = EssentialsMod.getInstance().getRegistry().getModelHandler(ClaimHandler.class);
        if (handler.isChunkClaimed(chunk)) {
            if (handler.getClaimAllowed(chunk).stream().noneMatch((it)->it.getUser().equals(player.getUuid()))) {
                cir.setReturnValue(ActionResult.FAIL);
            }
        }
    }
}
