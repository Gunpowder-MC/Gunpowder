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

package io.github.nyliummc.essentials

import io.github.nyliummc.essentials.api.EssentialsMod
import io.github.nyliummc.essentials.api.EssentialsModule
import io.github.nyliummc.essentials.entities.FabricDynmapBlockStateMapper
import io.github.nyliummc.essentials.entities.FabricDynmapServer
import net.fabricmc.fabric.api.event.server.ServerStartCallback
import net.fabricmc.fabric.api.event.server.ServerStopCallback
import net.fabricmc.fabric.api.event.server.ServerTickCallback
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.MinecraftServer
import org.dynmap.DynmapCommonAPIListener
import org.dynmap.DynmapCore
import org.dynmap.common.BiomeMap
import java.io.File


class EssentialsDynmapModule : EssentialsModule {
    override val name = "dynmap"
    override val toggleable = true
    private var serverInterface: FabricDynmapServer? = null


    val core = DynmapCore()

    init {
        instance = this
    }

    override fun registerEvents() {
        ServerStartCallback.EVENT.register(ServerStartCallback(::startServer))
        ServerStopCallback.EVENT.register(ServerStopCallback(::stopServer))
        ServerTickCallback.EVENT.register(ServerTickCallback { serverInterface?.tick() })
    }

    private fun startServer(minecraftServer: MinecraftServer) {
        core.server = FabricDynmapServer(minecraftServer)

        // Using codeSource allows it to be used in both modular and fatjar impls
        val path = this::class.java.classLoader.getResource(".")!!.path
        println(path)
        core.pluginJarFile = File(path)
        core.dataFolder = File(EssentialsMod.instance.server.runDirectory.canonicalPath + "/dynmap")
        core.dataFolder.mkdirs()
        core.setMinecraftVersion(minecraftServer.version)
        core.setPluginVersion(
                // Get essentials version
                FabricLoader.getInstance().getModContainer("essentials-mod").get().metadata.version.friendlyString,
                "Fabric Essentials Module Dynmap")
        serverInterface = FabricDynmapServer(EssentialsMod.instance.server)
        core.server = serverInterface
        BiomeMap.loadWellKnownByVersion(core.dynmapPluginPlatformVersion)
        FabricDynmapBlockStateMapper.INSTANCE.init()

        val success = core.enableCore()
        if (!success) throw RuntimeException("Dynmap failed to initialize")

        serverInterface?.init(core)

        DynmapCommonAPIListener.apiInitialized(core);
    }

    private fun stopServer(minecraftServer: MinecraftServer) {
        DynmapCommonAPIListener.apiTerminated();
        core.disableCore()
        serverInterface = null
    }

    companion object {
        var instance: EssentialsDynmapModule? = null
    }
}
