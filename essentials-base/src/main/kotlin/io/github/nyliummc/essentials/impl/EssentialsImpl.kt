package io.github.nyliummc.essentials.impl

import io.github.nyliummc.essentials.api.Essentials
import io.github.nyliummc.essentials.api.extension.EssentialsExtension
import io.github.nyliummc.essentials.api.extension.ExtensionEntrypoint
import io.github.nyliummc.essentials.impl.registry.EssentialsRegistryImpl
import io.github.nyliummc.essentials.impl.user.UserManagerImpl
import net.fabricmc.loader.api.FabricLoader
import java.util.*

class EssentialsImpl private constructor() : Essentials {
    private val extensions: MutableMap<Class<*>?, EssentialsExtension> = IdentityHashMap()
    override val userManager = UserManagerImpl()
    override val registry = EssentialsRegistryImpl(this)
    override fun <T : EssentialsExtension?> getExtension(clazz: Class<T>?): Optional<T> {
        return Optional.ofNullable(extensions[clazz] as T)
    }

    fun <T : EssentialsExtension?> register(extensionClass: Class<T>?, instance: EssentialsExtension) {
        extensions[extensionClass] = instance
    }

    companion object {
        @JvmStatic
        var instance: EssentialsImpl? = null
            get() {
                if (field == null) {
                    field = EssentialsImpl()
                }
                return field
            }
            private set
    }

    init {
        // TODO: Permissions stuff
        // Load extensions
        val entrypoints = FabricLoader.getInstance().getEntrypoints("io.github.nyliummc.essentials:extensions", ExtensionEntrypoint::class.java)
        for (entrypoint in entrypoints) {
            entrypoint.registerBuilders(registry)
            entrypoint.registerExtensions<EssentialsExtension>(this, registry)
        }
    }
}