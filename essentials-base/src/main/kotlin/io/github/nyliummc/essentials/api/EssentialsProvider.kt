package io.github.nyliummc.essentials.api

import io.github.nyliummc.essentials.impl.EssentialsImpl

object EssentialsProvider {
    @JvmStatic
	fun get(): EssentialsImpl? {
        return EssentialsImpl.instance
    }
}