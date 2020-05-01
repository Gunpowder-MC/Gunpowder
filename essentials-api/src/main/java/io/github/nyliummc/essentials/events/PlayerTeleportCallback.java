package io.github.nyliummc.essentials.events;

import io.github.nyliummc.essentials.api.builders.TeleportRequest;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;

/**
 * Called before a teleport request is executed and was not cancelled by PlayerPreTeleportCallback.
 *
 * TODO: Support vanilla commands (maybe others too?)
 */
public interface PlayerTeleportCallback {
    Event<PlayerTeleportCallback> EVENT = EventFactory.createArrayBacked(PlayerTeleportCallback.class, (listeners) -> (player, request) -> {
        for (PlayerTeleportCallback l : listeners) {
            l.trigger(player, request);
        }
    });

    void trigger(ServerPlayerEntity player, TeleportRequest request);
}
