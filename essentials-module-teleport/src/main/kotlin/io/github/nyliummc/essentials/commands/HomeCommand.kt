package io.github.nyliummc.essentials.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.github.nyliummc.essentials.api.EssentialsMod
import io.github.nyliummc.essentials.api.builders.Command
import io.github.nyliummc.essentials.api.builders.TeleportRequest
import io.github.nyliummc.essentials.api.builders.Text
import io.github.nyliummc.essentials.api.module.teleport.dataholders.StoredHome
import io.github.nyliummc.essentials.api.module.teleport.modelhandlers.HomeHandler
import io.github.nyliummc.essentials.configs.TeleportConfig
import net.minecraft.server.command.CommandSource
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.LiteralText
import net.minecraft.util.Formatting
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import net.minecraft.world.dimension.DimensionType
import java.util.concurrent.CompletableFuture

object HomeCommand {
    val handler by lazy {
        EssentialsMod.instance.registry.getModelHandler(HomeHandler::class.java)
    }

    val teleportDelay by lazy {
        EssentialsMod.instance.registry.getConfig(TeleportConfig::class.java).teleportDelay
    }

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        Command.builder(dispatcher) {
            command("home") {
                executes(::execute)

                literal("list") {
                    executes(::executeList)
                }

                literal("set") {
                    executes(::executeSet)

                    argument("home", StringArgumentType.greedyString()) {
                        executes(::executeSetTarget)
                    }
                }

                argument("home", StringArgumentType.greedyString()) {
                    suggests(::suggestHomes)
                    executes(::executeTarget)
                }
            }

            command("delhome") {
                executes(::executeDel)

                argument("home", StringArgumentType.greedyString()) {
                    suggests(::suggestHomes)
                    executes(::executeDelTarget)
                }
            }

            command("sethome") {
                executes(::executeSet)

                argument("home", StringArgumentType.greedyString()) {
                    executes(::executeSetTarget)
                }
            }
        }
    }

    fun execute(context: CommandContext<ServerCommandSource>): Int {
        val player = context.source.player
        val home = handler.getHome(player.uuid, "home")
        if (home == null) {
            player.addChatMessage(LiteralText("No such home: 'home'"), false)
            return -1
        }

        TeleportRequest.builder {
            player(player)
            dimension(DimensionType.byRawId(home.dimension)!!)
            destination(Vec3d(home.location))
        }.execute(teleportDelay.toLong())

        return 1
    }

    fun executeTarget(context: CommandContext<ServerCommandSource>): Int {
        val player = context.source.player
        val homeName = StringArgumentType.getString(context, "home")
        val home = handler.getHome(player.uuid, homeName)
        if (home == null) {
            player.addChatMessage(LiteralText("No such home: '${homeName}'"), false)
            return -1
        }

        TeleportRequest.builder {
            player(player)
            dimension(DimensionType.byRawId(home.dimension)!!)
            destination(Vec3d(home.location))
        }.execute(teleportDelay.toLong())

        return 1
    }

    fun executeList(context: CommandContext<ServerCommandSource>): Int {
        val homes = handler.getHomes(context.source.player.uuid)
        val text = Text.builder {
            text("Homes:")
            homes.forEach {
                text("\n - ")
                text(it.key) {
                    onClickCommand("/home ${it.key}")
                    color(Formatting.YELLOW)
                    onHoverText("/home ${it.key}")
                }
            }
        }
        context.source.player.addChatMessage(text, false)
        return 1
    }

    fun executeSet(context: CommandContext<ServerCommandSource>): Int {
        val player = context.source.player
        if (handler.newHome(
                        StoredHome(
                                player.uuid,
                                "home",
                                Vec3i(player.posVector.x, player.posVector.y, player.posVector.z),
                                player.dimension.rawId
                        )
                )) {
            player.addChatMessage(LiteralText("Home 'home' set"), false)
            return 1
        }
        player.addChatMessage(LiteralText("Home 'home' already exists"), false)
        return -1
    }

    fun executeSetTarget(context: CommandContext<ServerCommandSource>): Int {
        val home = StringArgumentType.getString(context, "home")
        val player = context.source.player
        if (handler.newHome(
                        StoredHome(
                                player.uuid,
                                home,
                                Vec3i(player.posVector.x, player.posVector.y, player.posVector.z),
                                player.dimension.rawId
                        )
                )) {
            player.addChatMessage(LiteralText("Home '$home' set"), false)
            return 1
        }
        player.addChatMessage(LiteralText("Home '$home' already exists"), false)
        return -1
    }

    fun executeDel(context: CommandContext<ServerCommandSource>): Int {
        if (handler.delHome(context.source.player.uuid, "home")) {
            context.source.player.addChatMessage(LiteralText("Home 'home' deleted"), false)
            return 1
        }
        context.source.player.addChatMessage(LiteralText("No such home: 'home'"), false)
        return -1
    }

    fun executeDelTarget(context: CommandContext<ServerCommandSource>): Int {
        val home = StringArgumentType.getString(context, "home")
        if (handler.delHome(context.source.player.uuid, home)) {
            context.source.player.addChatMessage(LiteralText("Home '$home' deleted"), false)
            return 1
        }
        context.source.player.addChatMessage(LiteralText("No such home: '$home'"), false)
        return -1
    }

    fun suggestHomes(context: CommandContext<ServerCommandSource>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        // TODO: Autocomplete based on what we have
        val homes = handler.getHomes(context.source.player.uuid)
        return CommandSource.suggestMatching(homes.map { it.key }, builder)
    }
}
