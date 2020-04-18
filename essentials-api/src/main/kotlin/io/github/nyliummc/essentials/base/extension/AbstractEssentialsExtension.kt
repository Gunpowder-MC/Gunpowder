package io.github.nyliummc.essentials.base.extension

import io.github.nyliummc.essentials.api.Essentials
import io.github.nyliummc.essentials.api.extension.EssentialsExtension

abstract class AbstractEssentialsExtension protected constructor(override val name: String, override val essentials: Essentials) : EssentialsExtension