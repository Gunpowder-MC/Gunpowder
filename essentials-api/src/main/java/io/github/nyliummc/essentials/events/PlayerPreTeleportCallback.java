/*
 * MIT License
 *
 * Copyright (c) NyliumMC
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

package io.github.nyliummc.essentials.events;

import io.github.nyliummc.essentials.api.builders.TeleportRequest;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;

/**
 * Called before a teleport request is queued; Allows cancelling.
 * All callbacks are always called, but cannot force execution if another returned ActionResult.FAIL.
 * <p>
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
