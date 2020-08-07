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

import io.github.gunpowder.api.builders.TeleportRequest;
import io.github.gunpowder.events.PlayerPreTeleportCallback;
import io.github.gunpowder.events.PlayerTeleportCallback;
import net.minecraft.command.arguments.PosArgument;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TeleportCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(TeleportCommand.class)
public class TeleportCommandMixin_Base {
    @Inject(method="execute(Lnet/minecraft/server/command/ServerCommandSource;Ljava/util/Collection;Lnet/minecraft/entity/Entity;)I", at=@At("HEAD"))
    private static void teleportEvent(ServerCommandSource source, Collection<? extends Entity> targets, Entity destination, CallbackInfoReturnable<Integer> cir) {
        try {
            for (Entity e : targets) {
                if (e instanceof ServerPlayerEntity) {
                    ActionResult r = PlayerPreTeleportCallback.EVENT.invoker().trigger(
                            (ServerPlayerEntity) e,
                            TeleportRequest.builder((b) -> {
                                b.destination(destination.getPos());
                                b.dimension(destination.world);
                                b.player((ServerPlayerEntity) e);
                            })
                    );

                    if (r != ActionResult.FAIL) {
                        PlayerTeleportCallback.EVENT.invoker().trigger(
                                (ServerPlayerEntity) e,
                                TeleportRequest.builder((b) -> {
                                    b.destination(destination.getPos());
                                    b.dimension(destination.world);
                                    b.player((ServerPlayerEntity) e);
                                })
                        );
                    } else {
                        targets.remove(e);
                    }
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Inject(method="execute(Lnet/minecraft/server/command/ServerCommandSource;Ljava/util/Collection;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/command/arguments/PosArgument;Lnet/minecraft/command/arguments/PosArgument;Lnet/minecraft/server/command/TeleportCommand$LookTarget;)I", at=@At("HEAD"))
    private static void teleportEvent2(ServerCommandSource source, Collection<? extends Entity> targets, ServerWorld world, PosArgument location, PosArgument rotation, TeleportCommand.LookTarget facingLocation, CallbackInfoReturnable<Integer> cir) {
        try {
            for (Entity e : targets) {
                if (e instanceof ServerPlayerEntity) {
                    TeleportRequest request = TeleportRequest.builder((b) -> {
                        b.destination(location.toAbsolutePos(source));
                        b.dimension(world);
                        if (rotation != null) {
                            b.facing(rotation.toAbsoluteRotation(source));
                        }
                        b.player((ServerPlayerEntity) e);
                    });

                    ActionResult r = PlayerPreTeleportCallback.EVENT.invoker().trigger(
                            (ServerPlayerEntity) e,
                            request
                    );

                    if (r != ActionResult.FAIL) {
                        PlayerPreTeleportCallback.EVENT.invoker().trigger(
                                (ServerPlayerEntity) e,
                                request
                        );
                    } else {
                        targets.remove(e);
                    }
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}
