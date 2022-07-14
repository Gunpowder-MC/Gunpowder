package io.github.gunpowder.mixinterfaces

interface SyncPlayer {
    fun setNeedsSync(x: Boolean)
    fun needsSync(): Boolean
}
