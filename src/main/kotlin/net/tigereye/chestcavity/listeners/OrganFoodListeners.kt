package net.tigereye.chestcavity.listeners

import net.minecraft.world.entity.player.Player
import net.minecraft.world.food.FoodProperties
import net.minecraft.world.item.ItemStack
import net.tigereye.chestcavity.chestcavities.ChestCavityEntity
import net.tigereye.chestcavity.chestcavities.instance.ChestCavityInstance
import net.tigereye.chestcavity.registration.CCItems
import net.tigereye.chestcavity.registration.CCOrganScores
import net.tigereye.chestcavity.registration.CCStatusEffects
import net.tigereye.chestcavity.registration.CCTags
import net.tigereye.chestcavity.util.ChestCavityUtil

data class EffectiveFoodScores(var digestion: Float, var nutrition: Float)

object OrganFoodListeners {

    fun computeEffectiveScores(stack: ItemStack, food: FoodProperties, cc: ChestCavityInstance): EffectiveFoodScores {
        val efs = EffectiveFoodScores(
            digestion = cc.organScore(CCOrganScores.DIGESTION),
            nutrition = cc.organScore(CCOrganScores.NUTRITION)
        )
        applyHerbivorousCarnivorous(stack, cc, efs)
        applyRot(stack, cc, efs)
        applyFurnacePower(stack, cc, efs)
        return efs
    }

    fun modifyFoodValues(stack: ItemStack, food: FoodProperties, player: Player): Pair<Int, Float>? {
        val cce = ChestCavityEntity.of(player) ?: return null
        val cc = cce.chestCavityInstance
        if (!cc.opened) return null

        val efs = computeEffectiveScores(stack, food, cc)

        val baseHunger = food.nutrition()
        val baseSatMod = food.saturation()

        val saturationGain = ChestCavityUtil.applyNutrition(cc, efs.nutrition, baseSatMod) * baseHunger * 2f
        val hungerGain = ChestCavityUtil.applyDigestion(cc, efs.digestion, baseHunger)
        val newSatMod = if (hungerGain > 0) saturationGain / (hungerGain * 2f) else 0f

        return hungerGain to newSatMod
    }

    private fun applyHerbivorousCarnivorous(stack: ItemStack, cc: ChestCavityInstance, efs: EffectiveFoodScores) {
        val isMeat = stack.`is`(CCTags.CARNIVORE_FOOD)
        if (isMeat) {
            efs.digestion += cc.organScore(CCOrganScores.CARNIVOROUS_DIGESTION)
            efs.nutrition += cc.organScore(CCOrganScores.CARNIVOROUS_NUTRITION)
        } else {
            efs.digestion += cc.organScore(CCOrganScores.HERBIVOROUS_DIGESTION)
            efs.nutrition += cc.organScore(CCOrganScores.HERBIVOROUS_NUTRITION)
        }
    }

    private fun applyRot(stack: ItemStack, cc: ChestCavityInstance, efs: EffectiveFoodScores) {
        if (!stack.`is`(CCTags.ROTTEN_FOOD)) return
        efs.digestion += cc.organScore(CCOrganScores.ROT_DIGESTION)
        efs.nutrition += cc.organScore(CCOrganScores.ROTGUT)
    }

    private fun applyFurnacePower(stack: ItemStack, cc: ChestCavityInstance, efs: EffectiveFoodScores) {
        if (stack.item != CCItems.FURNACE_POWER.get()) return
        val power = cc.owner.getEffect(CCStatusEffects.FURNACE_POWER)?.amplifier?.plus(1) ?: 0
        // Undo herbivorous false positive
        efs.digestion -= cc.organScore(CCOrganScores.HERBIVOROUS_DIGESTION)
        efs.nutrition -= cc.organScore(CCOrganScores.HERBIVOROUS_NUTRITION)
        efs.nutrition += power
    }
}
