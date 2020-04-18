package io.github.nyliummc.essentials.api

import io.github.nyliummc.essentials.impl.EssentialsImpl

object EssentialsProvider {
    @JvmStatic
	fun get(): Essentials {
        return EssentialsImpl
    }
}