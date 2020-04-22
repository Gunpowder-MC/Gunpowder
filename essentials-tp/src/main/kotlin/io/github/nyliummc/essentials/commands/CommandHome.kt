package io.github.nyliummc.essentials.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.github.nyliummc.essentials.createCommands
import io.github.nyliummc.essentials.entities.StoredHome
import io.github.nyliummc.essentials.extension.teleport.api.Teleportation
import io.github.nyliummc.essentials.modelcache.HomeCache
import io.github.nyliummc.essentials.util.TPCache
import net.minecraft.server.command.CommandSource
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.util.Formatting
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import net.minecraft.world.dimension.DimensionType
import java.util.concurrent.CompletableFuture

object CommandHome {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        createCommands(dispatcher) {
            command("home") {
                executes(::execute)

                argument("list") {
                    executes(::executeList)
                }

                argument("set") {
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
        val home = HomeCache.getHome(player.gameProfile, "home")
        if (home == null) {
            // player.addChatMessage(ConfigText(EssentialsModKt.messageConfig.tp.home.failure, "home"), false)
            return -1
        }
        TPCache.setLastLocation(player.gameProfile, Vec3i(player.x, player.y, player.z), player.dimension.rawId)
        Teleportation.builder {
            dimension(DimensionType.byRawId(home.dimension)!!)
            destination(Vec3d(home.location))
            facing(player.rotationClient)
        }
        return 1
    }

    fun executeTarget(context: CommandContext<ServerCommandSource>): Int {
        val player = context.source.player
        val homeName = StringArgumentType.getString(context, "home")
        val home = HomeCache.getHome(player.gameProfile, homeName)
        if (home == null) {
            // player.addChatMessage(ConfigText(EssentialsModKt.messageConfig.tp.home.failure, homeName), false)
            return -1
        }
        TPCache.setLastLocation(player.gameProfile, Vec3i(player.x, player.y, player.z), player.dimension.rawId)
        Teleportation.builder {
            dimension(DimensionType.byRawId(home.dimension)!!)
            destination(Vec3d(home.location))
            facing(player.rotationClient)
        }
        return 1
    }

    fun executeList(context: CommandContext<ServerCommandSource>): Int {
        val homes = HomeCache.getHomes(context.source.player.gameProfile)
        /*
        val text = createText {
            text(EssentialsModKt.messageConfig.tp.listhome.success + "\n")
            homes.forEach {
                text(" - ")
                text(ConfigText(EssentialsModKt.messageConfig.tp.listhome.successAlt + "\n", it.key)) {
                    onClickCommand("/home ${it.key}")
                    color(Formatting.YELLOW)
                    onHoverText("/home ${it.key}")
                }
            }
        }
        context.source.player.addChatMessage(text, false)
        */
        return 1
    }

    fun executeSet(context: CommandContext<ServerCommandSource>): Int {
        val player = context.source.player
        if (HomeCache.newHome(
                        StoredHome(
                                player.gameProfile,
                                "home",
                                Vec3i(player.posVector.x, player.posVector.y, player.posVector.z),
                                player.dimension.rawId
                        )
                )) {
            // player.addChatMessage(ConfigText(EssentialsModKt.messageConfig.tp.sethome.success, "home"), false)
            return 1
        }
        // player.addChatMessage(ConfigText(EssentialsModKt.messageConfig.tp.sethome.failure, "home"), false)
        return -1
    }

    fun executeSetTarget(context: CommandContext<ServerCommandSource>): Int {
        val home = StringArgumentType.getString(context, "home")
        val player = context.source.player
        if (HomeCache.newHome(
                        StoredHome(
                                player.gameProfile,
                                home,
                                Vec3i(player.posVector.x, player.posVector.y, player.posVector.z),
                                player.dimension.rawId
                        )
                )) {
            // player.addChatMessage(ConfigText(EssentialsModKt.messageConfig.tp.sethome.success, home), false)
            return 1
        }
        // player.addChatMessage(ConfigText(EssentialsModKt.messageConfig.tp.sethome.failure, home), false)
        return -1
    }

    fun executeDel(context: CommandContext<ServerCommandSource>): Int {
        val home = "home"
        if (HomeCache.delHome(context.source.player.gameProfile, home)) {
            // context.source.player.addChatMessage(ConfigText(EssentialsModKt.messageConfig.tp.delhome.success, home), false)
            return 1
        }
        // context.source.player.addChatMessage(ConfigText(EssentialsModKt.messageConfig.tp.delhome.failure, home), false)
        return -1
    }

    fun executeDelTarget(context: CommandContext<ServerCommandSource>): Int {
        val home = StringArgumentType.getString(context, "home")
        if (HomeCache.delHome(context.source.player.gameProfile, home)) {
            // context.source.player.addChatMessage(ConfigText(EssentialsModKt.messageConfig.tp.delhome.success, home), false)
            return 1
        }
        // context.source.player.addChatMessage(ConfigText(EssentialsModKt.messageConfig.tp.delhome.failure, home), false)
        return -1
    }

    fun suggestHomes(context: CommandContext<ServerCommandSource>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        val homes = HomeCache.getHomes(context.source.player.gameProfile)
        return CommandSource.suggestMatching(homes.map { it.key }, builder)
    }
}
