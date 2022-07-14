package io.github.gunpowder.api.events

import net.fabricmc.fabric.api.event.EventFactory

object DatabaseEvents {
    fun interface Listener {
        fun trigger()
    }

    val DATABASE_READY = EventFactory.createArrayBacked(Listener::class.java) { cbs ->
        Listener {
            for (it in cbs) {
                it.trigger()
            }
        }
    }

    val DATABASE_CLOSED = EventFactory.createArrayBacked(Listener::class.java) { cbs ->
        Listener {
            for (it in cbs) {
                it.trigger()
            }
        }
    }
}
