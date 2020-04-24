package io.github.nyliummc.essentials.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import io.github.nyliummc.essentials.api.EssentialsMod
import io.github.nyliummc.essentials.api.builders.Command
import io.github.nyliummc.essentials.api.builders.Text
import io.github.nyliummc.essentials.api.modules.chat.modelhandlers.NicknameHandler
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.LiteralText

object NicknameCommand {
    private val maxLength = 32
    val handler by lazy {
        EssentialsMod.instance!!.registry.getModelHandler(NicknameHandler::class.java)
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
        val requestedNickname = StringArgumentType.getString(context, "nickname")
        if (requestedNickname.length > maxLength) {
            context.source.sendFeedback(Text.builder {
                text("Nickname too long") {
                    onHoverText("Limit set to $maxLength")
                }
            }, false)
            return 0
        }

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