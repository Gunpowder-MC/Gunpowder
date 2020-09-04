package io.github.gunpowder.events;


import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.List;

/**
 * Called BEFORE night is skipped. This does NOT guarantee all players in the current world are asleep.
 */
public interface WorldPreSleepCallback {
    Event<WorldPreSleepCallback> EVENT = EventFactory.createArrayBacked(WorldPreSleepCallback.class, (listeners) -> (world, players) -> {
        for (WorldPreSleepCallback l : listeners) {
            l.trigger(world, players);
        }
    });

    void trigger(ServerWorld world, List<ServerPlayerEntity> players);
}
