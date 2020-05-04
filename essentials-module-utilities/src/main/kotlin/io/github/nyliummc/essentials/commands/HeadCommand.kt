package io.github.nyliummc.essentials.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import io.github.nyliummc.essentials.api.builders.Command
import net.minecraft.command.arguments.EntityArgumentType
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.LiteralText
import net.minecraft.util.ItemScatterer


object HeadCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        Command.builder(dispatcher) {
            command("head") {
                argument("player", StringArgumentType.string()) {
                    requires { it.hasPermissionLevel(4) }
                    executes(::getSkull)

                    argument("amount", IntegerArgumentType.integer(1, 64)) {
                        requires { it.hasPermissionLevel(4) }
                        executes(::getSkullAmount)
                    }
                }
            }
        }
    }

    private fun getSkullAmount(context: CommandContext<ServerCommandSource>): Int {
        val player = StringArgumentType.getString(context, "player")
        val amount = IntegerArgumentType.getInteger(context, "amount")
        giveHead(context.source.player, player, amount)
        return 1

    }

    private fun getSkull(context: CommandContext<ServerCommandSource>): Int {
        val player = StringArgumentType.getString(context, "player")
        giveHead(context.source.player, player, 0)
        return 1
    }

    private fun giveHead(player: ServerPlayerEntity, targetPlayer: String, amount: Int) {
        val stack = ItemStack(Items.PLAYER_HEAD, amount)
        val compound = stack.orCreateTag
        compound.putString("SkullOwner", targetPlayer)
        stack.tag = compound

        if (player.giveItemStack(stack)) {
            // Unable to insert
            ItemScatterer.spawn(player.world, player.x, player.y, player.z, stack)
        }
    }
}
