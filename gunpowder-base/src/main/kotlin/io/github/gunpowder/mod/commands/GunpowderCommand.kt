package io.github.gunpowder.mod.commands

import com.mojang.brigadier.arguments.StringArgumentType
import io.github.gunpowder.api.types.Command
import io.github.gunpowder.api.GunpowderDatabase
import io.github.gunpowder.api.GunpowderMod
import io.github.gunpowder.api.GunpowderModule
import io.github.gunpowder.api.builders.CommandBuilderContext
import io.github.gunpowder.api.builders.argument
import io.github.gunpowder.api.ext.error
import io.github.gunpowder.api.ext.openGUI
import io.github.gunpowder.api.ext.showSidebar
import io.github.gunpowder.api.types.ChestGUI
import io.github.gunpowder.mod.tables.ModuleTable
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.jetbrains.exposed.sql.update
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit

object GunpowderCommand : Command, KoinComponent {
    private val mod by inject<GunpowderMod>()
    private val db by inject<GunpowderDatabase>()

    context(GunpowderModule, CommandBuilderContext)
    override fun build() {
        command("gunpowder") {
            executes {
                source.player.showSidebar {
                    title("Gunpowder", Formatting.BOLD)
                    line("Welcome to gunpowder!")
                    line("")
                    line("Modules loaded:")
                    scrolling {
                        for (module in mod.modules) {
                            line("- ${module.name}", if (module.enabled) Formatting.GREEN else Formatting.RED)
                        }
                        tick(500, ChronoUnit.MILLIS)
                    }
                    removeAfter(5)
                }

                0
            }

            literal("toggle") {
                argument("module", StringArgumentType.greedyString()) { module ->
                    executes {
                        val mod = mod.modules.firstOrNull { it.name == module() }

                        if (mod == null) {
                            error {
                                text("Module ${module()} does not exist!")
                            }
                            return@executes -1
                        }

                        if (!mod.toggleable) {
                            error {
                                text("Module ${mod.name} is not toggleable!")
                            }
                            return@executes -1
                        }

                        mod.enabled = !mod.enabled
                        db.transaction {
                            ModuleTable.update({ ModuleTable.id eq mod.name }) {
                                it[enabled] = mod.enabled
                            }
                        }

                        0
                    }

                    suggests { suggestionsBuilder ->
                        for (m in mod.modules) {
                            if (m.toggleable) {
                                suggestionsBuilder.suggest(m.name)
                            }
                        }
                        suggestionsBuilder.buildFuture()
                    }
                }
            }
        }
    }
}
