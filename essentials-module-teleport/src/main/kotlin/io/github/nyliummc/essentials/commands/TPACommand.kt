package io.github.nyliummc.essentials.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.github.nyliummc.essentials.api.EssentialsMod
import io.github.nyliummc.essentials.api.builders.Command
import io.github.nyliummc.essentials.api.builders.TeleportRequest
import io.github.nyliummc.essentials.api.builders.Text
import io.github.nyliummc.essentials.configs.TeleportConfig
import io.github.nyliummc.essentials.entities.TPACache
import net.minecraft.command.arguments.EntityArgumentType
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.LiteralText
import net.minecraft.util.Formatting
import java.util.concurrent.CompletableFuture

object TPACommand {
    val config by lazy {
        EssentialsMod.instance.registry.getConfig(TeleportConfig::class.java)
    }

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        Command.builder(dispatcher) {
            command("tpa") {
                argument("user", EntityArgumentType.player()) {
                    executes(::executeTpa)
                }
            }

            command("tpahere") {
                argument("user", EntityArgumentType.player()) {
                    executes(::executeTpahere)
                }
            }

            command("tpaccept") {
                executes {
                    executeTpaccept(it, true)
                }
                argument("user", EntityArgumentType.player()) {
                    suggests(::suggestTpaResponse)
                    executes {
                        executeTpaccept(it, false)
                    }
                }
            }

            command("tpdeny") {
                executes {
                    executeTpdeny(it, true)
                }
                argument("user", EntityArgumentType.player()) {
                    suggests(::suggestTpaResponse)
                    executes {
                        executeTpdeny(it, false)
                    }
                }
            }
        }
    }

    fun executeTpa(context: CommandContext<ServerCommandSource>): Int {
        val player = context.source.player
        val target = EntityArgumentType.getPlayer(context, "user")

        if (!TPACache.requestTpa(player, target) {
                    player.addChatMessage(LiteralText("TPA timed out"), false)
                }) {
            player.addChatMessage(LiteralText("Please specify a user"), false)
            return -1
        }

        player.addChatMessage(LiteralText("Requested TPA"), false)
        target.addChatMessage(Text.builder {
            text(player.gameProfile.name) {
                color(Formatting.RED)
            }
            text(" has requested to teleport to you. To accept, type ")
            text("/tpaccept") {
                color(Formatting.YELLOW)
                onClickCommand("/tpaccept ${player.displayName.asString()}")
                onHoverText("/tpaccept ${player.displayName.asString()}")
            }
            text(", to deny, type ")
            text("/tpdeny") {
                color(Formatting.YELLOW)
                onClickCommand("/tpdeny ${player.displayName.asString()}")
                onHoverText("/tpdeny ${player.displayName.asString()}")
            }
            text(". This request will time out in ${config.tpaTimeout} seconds.")
        }, false)
        return 1
    }

    fun executeTpahere(context: CommandContext<ServerCommandSource>): Int {
        val player = context.source.player
        val target = EntityArgumentType.getPlayer(context, "user")
        if (!TPACache.requestTpaHere(player, target) {
                    player.addChatMessage(LiteralText("TPA timed out"), false)
                }) {
            player.addChatMessage(LiteralText("Please specify a user"), false)
            return -1
        }
        player.addChatMessage(LiteralText("Requested TPA"), false)
        target.addChatMessage(Text.builder {
            text(player.gameProfile.name) {
                color(Formatting.RED)
            }
            text(" has requested you teleport to them. To accept, type ")
            text("/tpaccept") {
                color(Formatting.YELLOW)
                onClickCommand("/tpaccept ${player.displayName.asString()}")
                onHoverText("/tpaccept ${player.displayName.asString()}")
            }
            text(", to deny, type ")
            text("/tpdeny") {
                color(Formatting.YELLOW)
                onClickCommand("/tpdeny ${player.displayName.asString()}")
                onHoverText("/tpdeny ${player.displayName.asString()}")
            }
            text(". This request will time out in ${config.tpaTimeout} seconds.")
        }, false)
        return 1
    }

    fun suggestTpaResponse(context: CommandContext<ServerCommandSource>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        TPACache.cache.forEach {
            if (it.toPlayer(context.source.player)) {
                builder.suggest(it.requester().gameProfile.name)
            }
        }
        return builder.buildFuture()
    }

    fun executeTpaccept(context: CommandContext<ServerCommandSource>, findSource: Boolean): Int {
        TPACache.closeTpa(if (findSource) null else EntityArgumentType.getPlayer(context, "user"), context.source.player) {
            TeleportRequest.builder {
                player(it.requester())
                destination(it.targetLocationEntity.pos)
                dimension(it.targetLocationEntity.dimension)
            }.execute(config.teleportDelay.toLong())
            it.requester().addChatMessage(LiteralText("TPA accepted"), false)
        }
        return 1
    }

    fun executeTpdeny(context: CommandContext<ServerCommandSource>, findSource: Boolean): Int {
        TPACache.closeTpa(if (findSource) null else EntityArgumentType.getPlayer(context, "user"), context.source.player) {
            it.requester().addChatMessage(LiteralText("TPA denied"), false)
        }
        return 1
    }
}
