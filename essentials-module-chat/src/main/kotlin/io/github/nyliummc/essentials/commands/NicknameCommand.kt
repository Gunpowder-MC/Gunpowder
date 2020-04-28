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
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import io.github.nyliummc.essentials.api.EssentialsMod
import io.github.nyliummc.essentials.api.builders.Command
import io.github.nyliummc.essentials.api.builders.Text
import io.github.nyliummc.essentials.api.modules.chat.modelhandlers.NicknameHandler
import io.github.nyliummc.essentials.configs.ChatConfig
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.LiteralText

object NicknameCommand {
    private val maxLength = 32
    val config by lazy {
        EssentialsMod.instance.registry.getConfig(ChatConfig::class.java)
    }
    val handler by lazy { // TODO: Dependency Injection
        EssentialsMod.instance.registry.getModelHandler(NicknameHandler::class.java)
    }

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        Command.builder(dispatcher) {
            command("nickname", "nick") {
                executes(::clearNickname)

                argument("nickname", StringArgumentType.greedyString()) {
                    executes(::setNickname)
                }
            }
        }
    }

    private fun clearNickname(context: CommandContext<ServerCommandSource>): Int {
        context.source.player.customName = LiteralText("")
        context.source.player.isCustomNameVisible = false
        context.source.minecraftServer.playerManager.sendToAll(
                PlayerListS2CPacket(
                        PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME,
                        context.source.player))

        handler.modifyUser(context.source.player.uuid) {
            it.nickname = ""
            it
        }

        context.source.sendFeedback(LiteralText("Nickname reset."), false)
        return 1
    }

    private fun setNickname(context: CommandContext<ServerCommandSource>): Int {
        var requestedNickname = StringArgumentType.getString(context, "nickname")
        if (requestedNickname.length > config.maxNicknameLength) {
            context.source.sendFeedback(Text.builder {
                text("Nickname too long") {
                    onHoverText("Limit set to ${config.maxNicknameLength}")
                }
            }, false)
            return 0
        }

        requestedNickname += "§r"

        // TODO: Reapply on user join
        handler.modifyUser(context.source.player.uuid) {
            it.nickname = requestedNickname
            it
        }

        context.source.player.customName = LiteralText(requestedNickname)
        context.source.player.isCustomNameVisible = true
        context.source.minecraftServer.playerManager.sendToAll(
                PlayerListS2CPacket(
                        PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME,
                        context.source.player))
        context.source.sendFeedback(LiteralText("Nickname set to $requestedNickname"), false)
        return 1
    }
}
