package net.tigereye.chestcavity.registration

import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.ItemTags
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.tigereye.chestcavity.ChestCavity

object CCTags {
    val BUTCHERING_TOOL: TagKey<Item> = tag("butchering_tool")
    val ROTTEN_FOOD: TagKey<Item> = tag("rotten_food")
    val CARNIVORE_FOOD: TagKey<Item> = tag("carnivore_food")
    val SALVAGEABLE: TagKey<Item> = tag("salvageable")
    val IRON_REPAIR_MATERIAL: TagKey<Item> = tag("iron_repair_material")

    private fun tag(name: String): TagKey<Item> =
        TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ChestCavity.MODID, name))
}

object CCTagOrgans {
    val tagMap: Map<TagKey<Item>, Map<ResourceLocation, Float>> = mapOf(
        ItemTags.DOORS to mapOf(CCOrganScores.EASE_OF_ACCESS to 64f),
        ItemTags.TRAPDOORS to mapOf(CCOrganScores.EASE_OF_ACCESS to 64f),
    )
}
