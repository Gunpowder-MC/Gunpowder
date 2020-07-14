package io.github.nyliummc.essentials.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import io.github.nyliummc.essentials.api.EssentialsMod
import io.github.nyliummc.essentials.api.builders.Command
import io.github.nyliummc.essentials.api.module.claims.dataholders.StoredClaim
import io.github.nyliummc.essentials.api.module.claims.dataholders.StoredClaimAuthorized
import io.github.nyliummc.essentials.api.module.claims.modelhandlers.ClaimHandler
import io.github.nyliummc.essentials.configs.SimpleClaimsConfig
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.command.arguments.GameProfileArgumentType
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.util.math.ChunkPos

object ClaimCommand {
    private val config by lazy {
        EssentialsMod.instance.registry.getConfig(SimpleClaimsConfig::class.java)
    }

    private val handler by lazy {
        EssentialsMod.instance.registry.getModelHandler(ClaimHandler::class.java)
    }

    private val currencyPresent by lazy {
        FabricLoader.getInstance().isModLoaded("essentials-module-currency")
    }

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        Command.builder(dispatcher) {
            command("claim") {
                // Claim this chunk
                executes(::claimChunk)

                if (currencyPresent && config.price != 0.00) {
                    literal("confirm") {
                        // Confirm claim
                        executes(::confirmClaimChunk)
                    }
                }

                literal("owner") {
                    // Print owner of claim
                    executes(::chunkOwner)
                }

                literal("add") {
                    // Add player to claim
                    argument("player", GameProfileArgumentType.gameProfile()) {
                        executes(::allowChunkPlayer)
                    }
                }

                literal("remove") {
                    // Remove player from claim
                    argument("player", GameProfileArgumentType.gameProfile()) {
                        executes(::disallowChunkPlayer)
                    }
                }

                literal("delete") {
                    // Unclaim chunk
                    executes(::unclaimChunk)
                }
            }
        }
    }

    private fun claimChunk(context: CommandContext<ServerCommandSource>): Int {
        val chunk = getChunk(context)

        if (handler.isChunkClaimed(chunk)) {
            // Error, already claimed
            return 0
        }

        if (currencyPresent && config.price != 0.00) {
            // Ask confirm
            return 1
        } else {
            return confirmClaimChunk(context)
        }
    }

    private fun confirmClaimChunk(context: CommandContext<ServerCommandSource>): Int {
        val chunk = getChunk(context)

        if (handler.isChunkClaimed(chunk)) {
            // Error, claimed before us
            return 0
        }

        if (handler.getClaims(context.source.player.uuid).size >= config.maxClaimChunks) {
            // Error, max claims
            return 0
        }

        handler.createClaim(StoredClaim(
                context.source.player.uuid,
                chunk
        ))
        // Success
        return 1
    }

    private fun chunkOwner(context: CommandContext<ServerCommandSource>): Int {
        val chunk = getChunk(context)

        if (!handler.isChunkClaimed(chunk)) {
            // Error, no claim
            return 0
        }

        val owner = EssentialsMod.instance.server.userCache.getByUuid(handler.getClaim(chunk).owner)

        // Message
        return 1
    }

    private fun allowChunkPlayer(context: CommandContext<ServerCommandSource>): Int {
        val chunk = getChunk(context)
        val player = GameProfileArgumentType.getProfileArgument(context, "player").first()

        if (!handler.isChunkClaimed(chunk)) {
            // Error, no claim
            return 0
        }

        val cfg = handler.getClaim(chunk)
        if (cfg.owner != context.source.player.uuid) {
            // Error, not owner
            return 0
        }

        if (handler.getClaimAllowed(chunk).any { it.user == player.id }) {
            // Error, already allowed
            return 0
        }

        handler.addClaimAllowed(StoredClaimAuthorized(cfg, player.id))
        // Message
        return 1
    }

    private fun disallowChunkPlayer(context: CommandContext<ServerCommandSource>): Int {
        val chunk = getChunk(context)
        val player = GameProfileArgumentType.getProfileArgument(context, "player").first()

        if (!handler.isChunkClaimed(chunk)) {
            // Error, no claim
            return 0
        }

        val cfg = handler.getClaim(chunk)
        if (cfg.owner != context.source.player.uuid) {
            // Error, not owner
            return 0
        }

        if (handler.getClaimAllowed(chunk).none { it.user == player.id }) {
            // Error, already not allowed
            return 0
        }

        handler.removeClaimAllowed(handler.getClaimAllowed(chunk).first { it.user == player.id })
        // Message
        return 1
    }

    private fun unclaimChunk(context: CommandContext<ServerCommandSource>): Int {
        val chunk = getChunk(context)
        if (!handler.isChunkClaimed(chunk)) {
            // Error, no claim
            return 0
        }

        val cfg = handler.getClaim(chunk)
        if (cfg.owner != context.source.player.uuid) {
            // Error, not owner
            return 0
        }

        handler.getClaimAllowed(chunk).map(handler::removeClaimAllowed)
        handler.deleteClaim(chunk)

        // Message
        return 1
    }

    private fun getChunk(context: CommandContext<ServerCommandSource>): ChunkPos {
        return ChunkPos(context.source.player.blockPos)
    }
}
