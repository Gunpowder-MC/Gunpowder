package io.github.nyliummc.essentials.impl

import io.github.nyliummc.essentials.api.Essentials
import io.github.nyliummc.essentials.api.extension.EssentialsExtension
import io.github.nyliummc.essentials.api.extension.ExtensionEntrypoint
import io.github.nyliummc.essentials.api.permission.VanillaPermissionEngine
import io.github.nyliummc.essentials.commands.InfoCommand
import io.github.nyliummc.essentials.impl.permission.VanillaPermissionEngineImpl
import io.github.nyliummc.essentials.impl.registry.EssentialsRegistryImpl
import io.github.nyliummc.essentials.impl.user.UserManagerImpl
import net.fabricmc.fabric.api.registry.CommandRegistry
import net.fabricmc.loader.api.FabricLoader
import java.util.*

object EssentialsImpl : Essentials {
    private const val EXTENSION_ENTRYPOINT = "essentials:extensions"
    private val extensions: MutableMap<Class<*>?, EssentialsExtension> = IdentityHashMap()
    private val vanillaPermissionsEngine: VanillaPermissionEngine = VanillaPermissionEngineImpl(this)
    override val userManager = UserManagerImpl()
    override val registry = EssentialsRegistryImpl(this)
    override fun <T : EssentialsExtension> getExtension(clazz: Class<T>?): Optional<T> {
        @Suppress("UNCHECKED_CAST")
        return Optional.ofNullable(extensions[clazz!!] as T)
    }

    fun <T : EssentialsExtension?> register(extensionClass: Class<T>?, instance: EssentialsExtension) {
        extensions[extensionClass] = instance
    }

    init {
        // TODO: Permissions stuff
        // Load extensions
        val containers = FabricLoader.getInstance().getEntrypointContainers(EXTENSION_ENTRYPOINT, ExtensionEntrypoint::class.java)
        for (container in containers) {
            // TODO: Put in debug log which mods register which extensions?
            container.entrypoint.registerBuilders(registry)
            container.entrypoint.registerExtensions<EssentialsExtension>(this, registry)
        }

        CommandRegistry.INSTANCE.register(false, InfoCommand::register)
    }
}