package net.tigereye.chestcavity.listeners

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.AttributeInstance
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.tigereye.chestcavity.CCConfig
import net.tigereye.chestcavity.ChestCavity
import net.tigereye.chestcavity.chestcavities.instance.ChestCavityInstance
import net.tigereye.chestcavity.registration.CCOrganScores
import net.tigereye.chestcavity.registration.CCStatusEffects

object OrganUpdateListeners {

    private val APPENDIX_ID = ResourceLocation.fromNamespaceAndPath(ChestCavity.MODID, "appendix_luck")
    private val HEART_ID = ResourceLocation.fromNamespaceAndPath(ChestCavity.MODID, "heart_max_hp")
    private val MUSCLE_STRENGTH_ID = ResourceLocation.fromNamespaceAndPath(ChestCavity.MODID, "muscle_attack_damage")
    private val MUSCLE_SPEED_ID = ResourceLocation.fromNamespaceAndPath(ChestCavity.MODID, "muscle_movement_speed")
    private val SPINE_ATTACK_SPEED_ID = ResourceLocation.fromNamespaceAndPath(ChestCavity.MODID, "spine_attack_speed")
    private val SPINE_MOVEMENT_ID = ResourceLocation.fromNamespaceAndPath(ChestCavity.MODID, "spine_movement")
    private val KNOCKBACK_RESISTANCE_ID = ResourceLocation.fromNamespaceAndPath(ChestCavity.MODID, "knockback_resistance")

    fun onOrganUpdate(entity: LivingEntity, cc: ChestCavityInstance) {
        updateAppendix(entity, cc)
        updateHeart(entity, cc)
        updateStrength(entity, cc)
        updateSpeed(entity, cc)
        updateSpine(entity, cc)
        updateKnockbackResistance(entity, cc)
        updateIncompatibility(entity, cc)
    }

    private fun updateAppendix(entity: LivingEntity, cc: ChestCavityInstance) {
        if (cc.oldOrganScore(CCOrganScores.LUCK) == cc.organScore(CCOrganScores.LUCK)) return
        val att = entity.getAttribute(Attributes.LUCK) ?: return
        val diff = cc.organScore(CCOrganScores.LUCK) - cc.type.getDefaultOrganScore(CCOrganScores.LUCK)
        replaceModifier(att, APPENDIX_ID, diff * CCConfig.APPENDIX_LUCK.get().toFloat(), AttributeModifier.Operation.ADD_VALUE)
    }

    private fun updateHeart(entity: LivingEntity, cc: ChestCavityInstance) {
        if (cc.oldOrganScore(CCOrganScores.HEALTH) == cc.organScore(CCOrganScores.HEALTH)) return
        val att = entity.getAttribute(Attributes.MAX_HEALTH) ?: return
        val diff = cc.organScore(CCOrganScores.HEALTH) - cc.type.getDefaultOrganScore(CCOrganScores.HEALTH)
        replaceModifier(att, HEART_ID, diff * CCConfig.HEART_HP.get().toFloat(), AttributeModifier.Operation.ADD_VALUE)
    }

    private fun updateStrength(entity: LivingEntity, cc: ChestCavityInstance) {
        if (cc.oldOrganScore(CCOrganScores.STRENGTH) == cc.organScore(CCOrganScores.STRENGTH)) return
        val att = entity.getAttribute(Attributes.ATTACK_DAMAGE) ?: return
        val diff = cc.organScore(CCOrganScores.STRENGTH) - cc.type.getDefaultOrganScore(CCOrganScores.STRENGTH)
        replaceModifier(att, MUSCLE_STRENGTH_ID, diff * CCConfig.MUSCLE_STRENGTH.get().toFloat() / 8f, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
    }

    private fun updateSpeed(entity: LivingEntity, cc: ChestCavityInstance) {
        if (cc.oldOrganScore(CCOrganScores.SPEED) == cc.organScore(CCOrganScores.SPEED)) return
        val att = entity.getAttribute(Attributes.MOVEMENT_SPEED) ?: return
        val diff = cc.organScore(CCOrganScores.SPEED) - cc.type.getDefaultOrganScore(CCOrganScores.SPEED)
        replaceModifier(att, MUSCLE_SPEED_ID, diff * CCConfig.MUSCLE_SPEED.get().toFloat() / 8f, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
    }

    private fun updateSpine(entity: LivingEntity, cc: ChestCavityInstance) {
        if (cc.oldOrganScore(CCOrganScores.NERVES) == cc.organScore(CCOrganScores.NERVES)) return
        if (cc.type.getDefaultOrganScore(CCOrganScores.NERVES) == 0f) return

        val movAtt = entity.getAttribute(Attributes.MOVEMENT_SPEED)
        if (movAtt != null) {
            val value = if (cc.organScore(CCOrganScores.NERVES) > 0) 0.0 else -1.0
            replaceModifier(movAtt, SPINE_MOVEMENT_ID, value.toFloat(), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
        }

        val atkAtt = entity.getAttribute(Attributes.ATTACK_SPEED)
        if (atkAtt != null) {
            val diff = cc.organScore(CCOrganScores.NERVES) - cc.type.getDefaultOrganScore(CCOrganScores.NERVES)
            replaceModifier(atkAtt, SPINE_ATTACK_SPEED_ID, diff * CCConfig.NERVES_HASTE.get().toFloat(), AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
        }
    }

    private fun updateKnockbackResistance(entity: LivingEntity, cc: ChestCavityInstance) {
        if (cc.oldOrganScore(CCOrganScores.KNOCKBACK_RESISTANT) == cc.organScore(CCOrganScores.KNOCKBACK_RESISTANT)) return
        val att = entity.getAttribute(Attributes.KNOCKBACK_RESISTANCE) ?: return
        val diff = cc.organScore(CCOrganScores.KNOCKBACK_RESISTANT) - cc.type.getDefaultOrganScore(CCOrganScores.KNOCKBACK_RESISTANT)
        replaceModifier(att, KNOCKBACK_RESISTANCE_ID, diff * 0.1f, AttributeModifier.Operation.ADD_VALUE)
    }

    private fun updateIncompatibility(entity: LivingEntity, cc: ChestCavityInstance) {
        if (cc.oldOrganScore(CCOrganScores.INCOMPATIBILITY) == cc.organScore(CCOrganScores.INCOMPATIBILITY)) return
        entity.removeEffect(CCStatusEffects.ORGAN_REJECTION)
    }

    private fun replaceModifier(att: AttributeInstance, id: ResourceLocation, amount: Float, op: AttributeModifier.Operation) {
        att.removeModifier(id)
        att.addPermanentModifier(AttributeModifier(id, amount.toDouble(), op))
    }
}
