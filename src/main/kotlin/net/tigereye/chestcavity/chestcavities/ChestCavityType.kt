package net.tigereye.chestcavity.chestcavities

import net.minecraft.resources.ResourceLocation
import net.minecraft.util.RandomSource
import net.minecraft.world.item.ItemStack
import net.tigereye.chestcavity.chestcavities.instance.ChestCavityInstance
import net.tigereye.chestcavity.chestcavities.organs.OrganData

interface ChestCavityType {
    val defaultOrganScores: Map<ResourceLocation, Float>
    val heartBleedCap: Float

    fun getDefaultOrganScore(id: ResourceLocation): Float =
        defaultOrganScores.getOrDefault(id, 0f)

    fun getDefaultChestCavity(): ChestCavityInventory
    fun isSlotForbidden(index: Int): Boolean
    fun fillChestCavityInventory(chestCavity: ChestCavityInventory)
    fun loadBaseOrganScores(organScores: MutableMap<ResourceLocation, Float>)
    fun catchExceptionalOrgan(slot: ItemStack): OrganData?
    fun generateLootDrops(random: RandomSource, looting: Int): List<ItemStack>
    fun setOrganCompatibility(instance: ChestCavityInstance)
    fun isOpenable(instance: ChestCavityInstance): Boolean
    fun onDeath(instance: ChestCavityInstance)
}
