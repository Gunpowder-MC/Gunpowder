package io.github.nyliummc.essentials.api

interface EssentialsModule {
    val name: String
    val essentials: EssentialsMod
    val toggleable: Boolean
    fun onInitialize()
}
