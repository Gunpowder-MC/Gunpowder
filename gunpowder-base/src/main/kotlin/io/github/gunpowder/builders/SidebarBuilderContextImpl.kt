package io.github.gunpowder.builders

import io.github.gunpowder.api.GunpowderMod
import io.github.gunpowder.api.GunpowderScheduler
import io.github.gunpowder.api.builders.SidebarBuilderContext
import io.github.gunpowder.api.types.TimeUnit
import io.github.gunpowder.api.types.plus
import net.minecraft.network.Packet
import net.minecraft.network.packet.s2c.play.ScoreboardDisplayS2CPacket
import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket
import net.minecraft.network.packet.s2c.play.ScoreboardPlayerUpdateS2CPacket
import net.minecraft.scoreboard.ScoreboardCriterion
import net.minecraft.scoreboard.ScoreboardObjective
import net.minecraft.scoreboard.ServerScoreboard
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit

object SidebarBuilderContextImpl : SidebarBuilderContext, KoinComponent {
    private val mod by inject<GunpowderMod>()
    private val scheduler by inject<GunpowderScheduler>()

    data class SidebarInfo(
        private val objective: ScoreboardObjective,
        private val player: ServerPlayerEntity,
        private val parts: List<Part>,
        private val tick: TimeUnit,
        private val removeAfter: TimeUnit
    ) {
        private var currentLines = mutableListOf<String>()
        fun start() {
            val removeTime = LocalDateTime.now().plus(removeAfter)
            show()
            scheduler.schedule(interval = tick, check = { LocalDateTime.now() < removeTime }, onComplete = ::remove) {
                tick()
            }
        }

        private fun show() {
            val packets = listOf<Packet<*>>(
                ScoreboardObjectiveUpdateS2CPacket(objective, ScoreboardObjectiveUpdateS2CPacket.ADD_MODE),
                ScoreboardDisplayS2CPacket(1, objective),
            )

            for (p in packets) {
                player.networkHandler.sendPacket(p)
            }
        }

        private fun tick() {
            val newLines = mutableListOf<String>()
            for (p in parts) {
                p.tick()
                newLines.addAll(p.getLines().map { it.second.toString() + it.first })
            }

            val max = minOf(15, newLines.size)
            val packets = mutableListOf<ScoreboardPlayerUpdateS2CPacket>()

            for (l in currentLines) {
                if (l !in newLines) {
                    packets.add(ScoreboardPlayerUpdateS2CPacket(ServerScoreboard.UpdateMode.REMOVE, objective.name, l, 0))
                }
            }

            for (line in max downTo 1) {
                packets.add(ScoreboardPlayerUpdateS2CPacket(ServerScoreboard.UpdateMode.CHANGE, objective.name, newLines[newLines.size - line], line))
            }

            for (p in packets) {
                player.networkHandler.sendPacket(p)
            }

            currentLines = newLines
        }

        private fun remove() {
            val packets = listOf<Packet<*>>(
                ScoreboardObjectiveUpdateS2CPacket(objective, ScoreboardObjectiveUpdateS2CPacket.REMOVE_MODE),
                ScoreboardDisplayS2CPacket(1, null), // 1 = sidebar
                *currentLines.map {
                    ScoreboardPlayerUpdateS2CPacket(ServerScoreboard.UpdateMode.REMOVE, objective.name, it, 0)
                }.toTypedArray()
            )

            for (p in packets) {
                player.networkHandler.sendPacket(p)
            }
        }
    }

    data class Part(
        private val lines: List<Pair<() -> String, Formatting>>,
        private val scrollable: Boolean,
        private val maxSize: Int,
        private val remainingSize: Int,
    ) {
        private var realLines = listOf<Pair<String, Formatting>>()
        private var startOffset = 0
        private val realSize = minOf(if (maxSize > remainingSize || maxSize < 0) remainingSize else maxSize, lines.size)

        fun tick() {
            if (scrollable) {
                startOffset = (startOffset + 1) % (lines.size - realSize + 1)
            }
            realLines = lines.map { it.first() to it.second }
        }

        fun getLines(): List<Pair<String, Formatting>> {
            return if (scrollable) {
                if (realSize > 0) {
                    realLines.subList(startOffset, startOffset + realSize)
                } else {
                    emptyList()
                }
            } else {
                realLines
            }
        }
    }

    override fun build(block: SidebarBuilderContext.SidebarBuilder.() -> Unit) {
        SidebarBuilderImpl().apply(block).build().start()
    }

    class SidebarBuilderImpl : SidebarBuilderContext.SidebarBuilder {
        private lateinit var player: ServerPlayerEntity
        private var title: Text = LiteralText("Menu")
        private var lines = mutableListOf<Pair<() -> String, Formatting>>()
        private val parts = mutableListOf<Part>()
        private var removeAfter = TimeUnit.NEVER
        private var tick = TimeUnit.NEVER
        private var n = 0

        override fun bind(player: ServerPlayerEntity) {
            this.player = player
        }

        override fun title(title: String, formatting: Formatting?) {
            this.title = LiteralText(title).styled {
                formatting?.let(it::withFormatting)
            }
        }

        override fun line(text: String, formatting: Formatting?) {
            n++
            lines.add(Pair({ text }, formatting ?: Formatting.WHITE))
        }

        override fun line(text: () -> String, formatting: Formatting?) {
            n++
            lines.add(Pair(text, formatting ?: Formatting.WHITE))
        }

        override fun scrolling(size: Int, block: SidebarBuilderContext.ScrollingSidebarBuilder.() -> Unit) {
            parts.add(Part(lines, false, -1, 15 - n))
            lines = mutableListOf()
            parts.add(ScrollingSidebarBuilderImpl(n, size).apply(block).build())
            if (size > 0) {
                n += size
            }
        }

        override fun removeAfter(duration: Long, unit: TemporalUnit) {
            removeAfter = TimeUnit(duration, unit)
        }

        override fun removeAfter(unit: TimeUnit) {
            removeAfter = unit
        }

        override fun tick(duration: Long, unit: TemporalUnit) {
            tick = TimeUnit(duration, unit)
        }

        override fun tick(unit: TimeUnit) {
            tick = unit
        }

        fun build(): SidebarInfo {
            val scoreboard = mod.server.scoreboard
            val objective = ScoreboardObjective(
                scoreboard, title.asString(),
                ScoreboardCriterion.DUMMY,
                title,
                ScoreboardCriterion.RenderType.INTEGER
            )

            if (lines.isNotEmpty()) {
                parts.add(Part(lines, false, -1, 15 - n))
            }

            return SidebarInfo(
                objective,
                player,
                parts,
                tick,
                removeAfter
            )
        }
    }

    class ScrollingSidebarBuilderImpl(private val n: Int, private val maxSize: Int) : SidebarBuilderContext.ScrollingSidebarBuilder {
        private val lines = mutableListOf<Pair<() -> String, Formatting>>()

        override fun line(text: String, formatting: Formatting?) {
            lines.add(Pair({ text }, formatting ?: Formatting.WHITE))
        }

        override fun line(text: () -> String, formatting: Formatting?) {
            lines.add(Pair(text, formatting ?: Formatting.WHITE))
        }

        fun build(): Part {
            return Part(lines, true, maxSize, 15 - n)
        }
    }
}
