/*
 * MIT License
 *
 * Copyright (c) GunpowderMC
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

package io.github.gunpowder.entities.builders

import io.github.gunpowder.api.GunpowderMod
import net.minecraft.network.Packet
import net.minecraft.network.packet.s2c.play.ScoreboardDisplayS2CPacket
import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket
import net.minecraft.network.packet.s2c.play.ScoreboardPlayerUpdateS2CPacket
import net.minecraft.scoreboard.ScoreboardCriterion
import net.minecraft.scoreboard.ScoreboardObjective
import net.minecraft.scoreboard.ServerScoreboard
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.LiteralText
import net.minecraft.util.Formatting
import io.github.gunpowder.api.builders.SidebarInfo as APISidebarInfo


class SidebarInfo(private val objective: ScoreboardObjective, private val player: ServerPlayerEntity, private val lines: List<Pair<String, Formatting>>) : APISidebarInfo {
    override fun remove() {
        val packets = mutableListOf<Packet<*>>(
            ScoreboardObjectiveUpdateS2CPacket(objective, 1),  // mode = remove
            ScoreboardDisplayS2CPacket(1, null),  // 1 = sidebar
            *lines.map {
                ScoreboardPlayerUpdateS2CPacket(ServerScoreboard.UpdateMode.REMOVE, objective.name, it.second.toString() + it.first, 0)
            }.toTypedArray()
        )

        packets.forEach(player.networkHandler::sendPacket)
    }

    class Builder : APISidebarInfo.Builder {
        private var title = "Menu"
        private var titleColor = Formatting.RESET
        private val lines = mutableListOf<Pair<String, Formatting>>()
        override fun title(text: String, color: Formatting) {
            title = text
            titleColor = color
        }

        override fun line(text: String, color: Formatting) {
            lines.add(Pair(text, color))
        }

        override fun build(player: ServerPlayerEntity): APISidebarInfo {
            val scoreboard = GunpowderMod.instance.server.scoreboard
            val objective = ScoreboardObjective(
                    scoreboard, title,
                    ScoreboardCriterion.DUMMY,
                    LiteralText(titleColor.toString() + title),
                    ScoreboardCriterion.RenderType.INTEGER
            )

            val packets = mutableListOf<Packet<*>>(
                ScoreboardObjectiveUpdateS2CPacket(objective, 0),  // mode = remove
                ScoreboardDisplayS2CPacket(1, objective),  // 1 = sidebar
                *lines.mapIndexed { index, pair ->
                    ScoreboardPlayerUpdateS2CPacket(ServerScoreboard.UpdateMode.CHANGE, title, pair.second.toString() + pair.first, lines.size - index)
                }.toTypedArray()
            )

            packets.forEach(player.networkHandler::sendPacket)

            return SidebarInfo(objective, player, lines)
        }

    }
}
