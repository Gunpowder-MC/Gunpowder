/*
 * MIT License
 *
 * Copyright (c) NyliumMC
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

package io.github.nyliummc.essentials.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import io.github.nyliummc.essentials.api.EssentialsMod
import io.github.nyliummc.essentials.api.builders.Command
import io.github.nyliummc.essentials.api.module.claims.dataholders.StoredClaim
import io.github.nyliummc.essentials.api.module.claims.dataholders.StoredClaimAuthorized
import io.github.nyliummc.essentials.api.module.claims.modelhandlers.ClaimHandler
import io.github.nyliummc.essentials.api.module.currency.modelhandlers.BalanceHandler
import io.github.nyliummc.essentials.configs.SimpleClaimsConfig
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.command.arguments.GameProfileArgumentType
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.LiteralText
import net.minecraft.util.math.ChunkPos

object ClaimCommand {
    private val config by lazy {
        EssentialsMod.instance.registry.getConfig(SimpleClaimsConfig::class.java)
    }

    private val handler by lazy {
        EssentialsMod.instance.registry.getModelHandler(ClaimHandler::class.java)
    }

    private val balanceHandler by lazy {
        EssentialsMod.instance.registry.getModelHandler(BalanceHandler::class.java)
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

        if (handler.isChunkClaimed(chunk, context.source.world.registryKey)) {
            // Error, already claimed
            context.source.sendFeedback(LiteralText("Chunk is already claimed!"), false)
            return 0
        }

        return if (currencyPresent && config.price != 0.00) {
            // Ask confirm
            context.source.sendFeedback(LiteralText("Claiming costs $${config.price} please confirm claiming this chunk with /claim confirm"), false)
            1
        } else {
            confirmClaimChunk(context)
        }
    }

    private fun confirmClaimChunk(context: CommandContext<ServerCommandSource>): Int {
        val chunk = getChunk(context)

        if (handler.isChunkClaimed(chunk, context.source.world.registryKey)) {
            context.source.sendFeedback(LiteralText("Chunk is already claimed!"), false)
            return 0
        }

        if (handler.getClaims(context.source.player.uuid).size >= config.maxClaimChunks) {
            context.source.sendFeedback(LiteralText("You've already claimed the maximum amount of chunks!"), false)
            return 0
        }

        if (currencyPresent && config.price != 0.00) {
            val account = balanceHandler.getUser(context.source.player.uuid)
            if (account.balance.toDouble() < config.price) {
                context.source.sendFeedback(LiteralText("Not enough money! Claiming a chunk costs ${config.price}"), false)
                return 0
            }
            account.balance -= config.price.toBigDecimal()
            balanceHandler.updateUser(account)
        }

        handler.createClaim(StoredClaim(
                context.source.player.uuid,
                chunk,
                context.source.world.registryKey
        ))
        context.source.sendFeedback(LiteralText("You are now the owner of this chunk"), false)
        return 1
    }

    private fun chunkOwner(context: CommandContext<ServerCommandSource>): Int {
        val chunk = getChunk(context)

        if (!handler.isChunkClaimed(chunk, context.source.world.registryKey)) {
            context.source.sendFeedback(LiteralText("This chunk is unclaimed"), false)
            return 0
        }

        val owner = EssentialsMod.instance.server.userCache.getByUuid(handler.getClaim(chunk, context.source.world.registryKey).owner)

        context.source.sendFeedback(LiteralText("Chunk owner: ${owner!!.name}"), false)
        return 1
    }

    private fun allowChunkPlayer(context: CommandContext<ServerCommandSource>): Int {
        val chunk = getChunk(context)
        val player = GameProfileArgumentType.getProfileArgument(context, "player").first()

        if (!handler.isChunkClaimed(chunk, context.source.world.registryKey)) {
            context.source.sendFeedback(LiteralText("This chunk is unclaimed"), false)
            return 0
        }

        val cfg = handler.getClaim(chunk, context.source.world.registryKey)
        if (cfg.owner != context.source.player.uuid) {
            context.source.sendFeedback(LiteralText("You are not the owner of this chunk"), false)
            return 0
        }

        if (handler.getClaimAllowed(chunk, context.source.world.registryKey).any { it.user == player.id }) {
            context.source.sendFeedback(LiteralText("This person already has permission to use this chunk"), false)
            return 0
        }

        handler.addClaimAllowed(StoredClaimAuthorized(cfg, player.id))
        context.source.sendFeedback(LiteralText("${player.name} is now permitted to use this chunk"), false)
        return 1
    }

    private fun disallowChunkPlayer(context: CommandContext<ServerCommandSource>): Int {
        val chunk = getChunk(context)
        val player = GameProfileArgumentType.getProfileArgument(context, "player").first()

        if (!handler.isChunkClaimed(chunk, context.source.world.registryKey)) {
            context.source.sendFeedback(LiteralText("This chunk is unclaimed"), false)
            return 0
        }

        val cfg = handler.getClaim(chunk, context.source.world.registryKey)
        if (cfg.owner != context.source.player.uuid) {
            context.source.sendFeedback(LiteralText("You are not the owner of this chunk"), false)
            return 0
        }

        if (handler.getClaimAllowed(chunk, context.source.world.registryKey).none { it.user == player.id }) {
            context.source.sendFeedback(LiteralText("This person already has no permission to use this chunk"), false)
            return 0
        }

        handler.removeClaimAllowed(handler.getClaimAllowed(chunk, context.source.world.registryKey).first { it.user == player.id })
        context.source.sendFeedback(LiteralText("${player.name} is no longer permitted to use this chunk"), false)
        return 1
    }

    private fun unclaimChunk(context: CommandContext<ServerCommandSource>): Int {
        val chunk = getChunk(context)
        if (!handler.isChunkClaimed(chunk, context.source.world.registryKey)) {
            context.source.sendFeedback(LiteralText("This chunk is unclaimed"), false)
            return 0
        }

        val cfg = handler.getClaim(chunk, context.source.world.registryKey)
        if (cfg.owner != context.source.player.uuid) {
            context.source.sendFeedback(LiteralText("You are not the owner of this chunk"), false)
            return 0
        }

        handler.getClaimAllowed(chunk, context.source.world.registryKey).map(handler::removeClaimAllowed)
        handler.deleteClaim(chunk, context.source.world.registryKey)

        context.source.sendFeedback(LiteralText("This chunk is now available to anyone"), false)
        return 1
    }

    private fun getChunk(context: CommandContext<ServerCommandSource>): ChunkPos {
        return ChunkPos(context.source.player.blockPos)
    }
}
