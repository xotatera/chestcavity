package net.tigereye.chestcavity.items

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag

class CreeperAppendix(properties: Properties) : Item(properties) {

    override fun appendHoverText(stack: ItemStack, context: TooltipContext, tooltip: MutableList<Component>, flag: TooltipFlag) {
        super.appendHoverText(stack, context, tooltip, flag)
        tooltip.add(Component.literal("This appears to be a fuse.").withStyle(ChatFormatting.ITALIC))
        tooltip.add(Component.literal("It won't do much by itself.").withStyle(ChatFormatting.ITALIC))
    }
}
