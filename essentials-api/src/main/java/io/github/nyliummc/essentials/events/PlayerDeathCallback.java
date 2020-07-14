package io.github.nyliummc.essentials.events;

import io.github.nyliummc.essentials.api.builders.TeleportRequest;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;

/**
 * Called after a player dies.
 */
public interface PlayerDeathCallback {
    Event<PlayerDeathCallback> EVENT = EventFactory.createArrayBacked(PlayerDeathCallback.class, (listeners) -> (player, source) -> {
        for (PlayerDeathCallback l : listeners) {
            l.trigger(player, source);
        }
    });

    void trigger(ServerPlayerEntity player, DamageSource source);
}
