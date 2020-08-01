package io.github.nyliummc.essentials.entities.builders

import io.github.nyliummc.essentials.api.EssentialsMod
import io.github.nyliummc.essentials.api.builders.Text
import net.minecraft.scoreboard.ServerScoreboard
import net.minecraft.server.network.ServerPlayerEntity
import io.github.nyliummc.essentials.api.builders.SidebarInfo as APISidebarInfo

class SidebarInfo : APISidebarInfo {


    class Builder : APISidebarInfo.Builder {
        private val lines = mutableListOf<Text>()

        override fun addLine(text: Text) {
            lines.add(text)
        }

        override fun build(player: ServerPlayerEntity) {
            // TODO:
            // - Create new ServerScoreboard (+ Objective?)
            // - Add players for each line in `lines`
            // - Send to player
            TODO("Not yet implemented")
        }

    }
}
