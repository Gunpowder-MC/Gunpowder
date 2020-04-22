package io.github.nyliummc.essentials.api

import io.github.nyliummc.essentials.api.extension.EssentialsExtension
import io.github.nyliummc.essentials.api.user.UserManager
import java.util.*

interface Essentials {
    fun <T : EssentialsExtension> getExtension(clazz: Class<T>?): Optional<T>
    val userManager: UserManager
    val registry: EssentialsRegistry
    val MOD_ID: String
}
