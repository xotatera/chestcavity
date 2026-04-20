package net.tigereye.chestcavity.events

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.network.chat.Component
import net.tigereye.chestcavity.chestcavities.ChestCavityEntity
import net.tigereye.chestcavity.util.ChestCavityUtil

object CCCommands {

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("chestcavity")
                .then(Commands.literal("getscores")
                    .executes(::getScoresSelf)
                    .then(Commands.argument("entity", EntityArgument.entity())
                        .executes(::getScoresTarget)))
                .then(Commands.literal("resetChestCavity")
                    .requires { it.hasPermission(2) }
                    .executes(::resetSelf)
                    .then(Commands.argument("entity", EntityArgument.entity())
                        .executes(::resetTarget)))
        )
    }

    private fun getScoresSelf(ctx: CommandContext<CommandSourceStack>): Int {
        val entity = ctx.source.entity ?: run {
            ctx.source.sendFailure(Component.literal("No entity context"))
            return -1
        }
        val cce = ChestCavityEntity.of(entity) ?: return 0
        ChestCavityUtil.outputOrganScores(cce.chestCavityInstance) {
            ctx.source.sendSuccess({ Component.literal(it) }, false)
        }
        return 1
    }

    private fun getScoresTarget(ctx: CommandContext<CommandSourceStack>): Int {
        val entity = EntityArgument.getEntity(ctx, "entity")
        val cce = ChestCavityEntity.of(entity) ?: return 0
        ChestCavityUtil.outputOrganScores(cce.chestCavityInstance) {
            ctx.source.sendSuccess({ Component.literal(it) }, false)
        }
        return 1
    }

    private fun resetSelf(ctx: CommandContext<CommandSourceStack>): Int {
        val entity = ctx.source.entity ?: run {
            ctx.source.sendFailure(Component.literal("No entity context"))
            return -1
        }
        val cce = ChestCavityEntity.of(entity) ?: return 0
        ChestCavityUtil.generateChestCavityIfOpened(cce.chestCavityInstance)
        return 1
    }

    private fun resetTarget(ctx: CommandContext<CommandSourceStack>): Int {
        val entity = EntityArgument.getEntity(ctx, "entity")
        val cce = ChestCavityEntity.of(entity) ?: return 0
        ChestCavityUtil.generateChestCavityIfOpened(cce.chestCavityInstance)
        return 1
    }
}
