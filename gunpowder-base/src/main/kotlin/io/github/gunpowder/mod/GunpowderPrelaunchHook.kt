package io.github.gunpowder.mod

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint

class GunpowderPrelaunchHook : PreLaunchEntrypoint {
    override fun onPreLaunch() {
        try {
            PrelaunchHook.moveToKnot("com.mojang.brigadier.Message")
        } catch (e: Exception) {
            throw IllegalStateException("Unable to initialize Brigadier Mixins, will not be able to inject")
        }
    }
}
