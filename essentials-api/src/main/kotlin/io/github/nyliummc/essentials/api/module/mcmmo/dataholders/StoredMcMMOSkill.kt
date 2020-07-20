package io.github.nyliummc.essentials.api.module.mcmmo.dataholders

import io.github.nyliummc.essentials.api.module.mcmmo.modelhandlers.McMMOSkillRegistry
import java.util.*

data class StoredMcMMOSkill(
        val user: UUID,
        val skill: McMMOSkillRegistry.SkillKey,
        var skillExp: Int,
        var skillLevel: Int
)
