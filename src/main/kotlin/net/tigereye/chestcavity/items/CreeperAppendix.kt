package net.tigereye.chestcavity.items

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.tigereye.chestcavity.ChestCavity

class CreeperAppendix(properties: Properties) : Item(properties) {

    override fun appendHoverText(stack: ItemStack, context: TooltipContext, tooltip: MutableList<Component>, flag: TooltipFlag) {
        super.appendHoverText(stack, context, tooltip, flag)
        tooltip.add(
            Component.translatable("text.${ChestCavity.MODID}.tooltip.creeper_appendix.fuse")
                .withStyle(ChatFormatting.ITALIC)
        )
        tooltip.add(
            Component.translatable("text.${ChestCavity.MODID}.tooltip.creeper_appendix.by_itself")
                .withStyle(ChatFormatting.ITALIC)
        )
    }
}
