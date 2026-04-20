package net.tigereye.chestcavity.chestcavities.organs

import net.minecraft.resources.ResourceLocation

data class OrganData(
    val pseudoOrgan: Boolean = false,
    val organScores: Map<ResourceLocation, Float> = emptyMap()
)
