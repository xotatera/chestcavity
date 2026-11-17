package net.tigereye.chestcavity.events

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent
import net.tigereye.chestcavity.ChestCavity
import net.tigereye.chestcavity.chestcavities.organs.OrganManager
import net.tigereye.chestcavity.registration.CCDataComponents
import net.tigereye.chestcavity.registration.CCEnchantments
import net.tigereye.chestcavity.registration.CCOrganScores
import net.tigereye.chestcavity.registration.OrganCompatibility

@EventBusSubscriber(modid = ChestCavity.MODID)
object CCItemEvents {

    @SubscribeEvent
    fun onItemTooltip(event: ItemTooltipEvent) {
        val stack = event.itemStack
        val tooltip = event.toolTip

        // Show organ scores — skip pseudo-organs unless it's a shulker box
        val componentData = stack.get(CCDataComponents.ORGAN_DATA.get())
        val organData = componentData?.toOrganData() ?: OrganManager.getEntry(stack)
        val isShulker = stack.item is net.minecraft.world.item.BlockItem &&
            (stack.item as net.minecraft.world.item.BlockItem).block is net.minecraft.world.level.block.ShulkerBoxBlock
        if (organData != null && (!organData.pseudoOrgan || isShulker)) {
            displayOrganQuality(organData.organScores, tooltip)
        }

        // Show compatibility (original OrganUtil#displayCompatibility): Curse of Malpractice makes the
        // organ unsafe; O Negative makes a bound organ safe for anyone.
        val compat = stack.get(CCDataComponents.ORGAN_COMPATIBILITY.get())
        val isTrueOrgan = organData != null && !organData.pseudoOrgan
        if (isTrueOrgan && CCEnchantments.level(stack, CCEnchantments.MALPRACTICE) > 0) {
            tooltip.add(
                Component.translatable("text.${ChestCavity.MODID}.tooltip.unsafe_to_use")
                    .withStyle(ChatFormatting.RED)
            )
        } else if (compat != null && CCEnchantments.level(stack, CCEnchantments.O_NEGATIVE) <= 0) {
            tooltip.add(
                Component.translatable("text.${ChestCavity.MODID}.tooltip.compatible_with", compat.ownerName)
                    .withStyle(ChatFormatting.RED)
            )
        } else if (isTrueOrgan) {
            tooltip.add(
                Component.translatable("text.${ChestCavity.MODID}.tooltip.safe_to_use")
                    .withStyle(ChatFormatting.GREEN)
            )
        }
    }

    private fun displayOrganQuality(scores: Map<ResourceLocation, Float>, tooltip: MutableList<Component>) {
        scores.forEach { (organ, score) ->
            val tier = when {
                organ == CCOrganScores.HYDROALLERGENIC && score >= 2 -> "quality.chestcavity.severely"
                score >= 1.5f -> "quality.chestcavity.supernatural"
                score >= 1.25f -> "quality.chestcavity.exceptional"
                score >= 1f -> "quality.chestcavity.good"
                score >= 0.75f -> "quality.chestcavity.average"
                score >= 0.5f -> "quality.chestcavity.poor"
                score >= 0f -> "quality.chestcavity.pathetic"
                score >= -0.25f -> "quality.chestcavity.slightly_reduces"
                score >= -0.5f -> "quality.chestcavity.reduces"
                else -> "quality.chestcavity.greatly_reduces"
            }
            tooltip.add(Component.translatable(
                "organscore.${organ.namespace}.${organ.path}",
                Component.translatable(tier)
            ))
        }
    }
}
