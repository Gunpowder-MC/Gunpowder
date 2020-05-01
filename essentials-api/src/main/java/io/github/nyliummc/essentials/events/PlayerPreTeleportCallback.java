package io.github.nyliummc.essentials.events;

import io.github.nyliummc.essentials.api.builders.TeleportRequest;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;

/**
 * Called before a teleport request is queued; Allows cancelling.
 * All callbacks are always called, but cannot force execution if another returned ActionResult.FAIL.
 *
 * TODO: Support vanilla commands (maybe others too?)
 */
public interface PlayerPreTeleportCallback {
    Event<PlayerPreTeleportCallback> EVENT = EventFactory.createArrayBacked(PlayerPreTeleportCallback.class, (listeners) -> (player, request) -> {
        ActionResult shouldPass = ActionResult.PASS;

        for (PlayerPreTeleportCallback l : listeners) {
            ActionResult r = l.trigger(player, request);

            if (r == ActionResult.FAIL) {
                shouldPass = ActionResult.FAIL;

            }
        }

        return shouldPass;
    });

    ActionResult trigger(ServerPlayerEntity player, TeleportRequest request);
}
