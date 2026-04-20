package net.tigereye.chestcavity.listeners

import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.monster.Creeper
import net.minecraft.world.entity.player.Player
import net.tigereye.chestcavity.CCConfig
import net.tigereye.chestcavity.chestcavities.instance.ChestCavityInstance
import net.tigereye.chestcavity.registration.CCOrganScores
import net.tigereye.chestcavity.registration.CCStatusEffects
import net.tigereye.chestcavity.util.ChestCavityUtil
import kotlin.math.max
import kotlin.math.min

object OrganTickListeners {

    fun tickAll(entity: LivingEntity, cc: ChestCavityInstance) {
        tickIncompatibility(entity, cc)
        tickProjectileQueue(entity, cc)
        tickHealth(entity, cc)
        tickFiltration(entity, cc)
        tickBuoyant(entity, cc)
        tickCrystalsynthesis(entity, cc)
        tickPhotosynthesis(entity, cc)
        tickHydroallergenic(entity, cc)
        tickHydrophobia(entity, cc)
        tickGlowing(entity, cc)
        tickSwimSpeed(entity, cc)
        tickCreeperFuse(entity, cc)
    }

    private fun tickIncompatibility(entity: LivingEntity, cc: ChestCavityInstance) {
        if (entity.level().isClientSide) return
        if (CCConfig.DISABLE_ORGAN_REJECTION.get()) return
        val incompatibility = cc.organScore(CCOrganScores.INCOMPATIBILITY)
        if (incompatibility <= 0) return
        if (entity.hasEffect(CCStatusEffects.ORGAN_REJECTION)) return
        entity.addEffect(MobEffectInstance(
            CCStatusEffects.ORGAN_REJECTION,
            (CCConfig.ORGAN_REJECTION_RATE.get() / incompatibility).toInt(),
            0, false, true, true
        ))
    }

    private fun tickProjectileQueue(entity: LivingEntity, cc: ChestCavityInstance) {
        if (cc.projectileCooldown > 0) {
            cc.projectileCooldown--
            return
        }
        if (cc.projectileQueue.isEmpty()) return
        cc.projectileCooldown = 5
        cc.projectileQueue.pop().accept(entity)
    }

    private fun tickHealth(entity: LivingEntity, cc: ChestCavityInstance) {
        val defaultHealth = cc.type.getDefaultOrganScore(CCOrganScores.HEALTH)
        if (defaultHealth == 0f) return
        if (cc.organScore(CCOrganScores.HEALTH) > 0) {
            cc.heartBleedTimer = 0
            return
        }
        if (entity.level().gameTime % CCConfig.HEARTBLEED_RATE.get() != 0L) return
        cc.heartBleedTimer++
        entity.hurt(entity.damageSources().starve(), min(cc.heartBleedTimer.toFloat(), cc.type.heartBleedCap))
    }

    private fun tickFiltration(entity: LivingEntity, cc: ChestCavityInstance) {
        if (entity.level().isClientSide) return
        val defaultFiltration = cc.type.getDefaultOrganScore(CCOrganScores.FILTRATION)
        if (defaultFiltration <= 0) return
        val ratio = cc.organScore(CCOrganScores.FILTRATION) / defaultFiltration
        if (ratio >= 1) return
        cc.bloodPoisonTimer++
        if (cc.bloodPoisonTimer < CCConfig.KIDNEY_RATE.get()) return
        entity.addEffect(MobEffectInstance(MobEffects.POISON, (48 * (1 - ratio)).toInt().coerceAtLeast(1)))
        cc.bloodPoisonTimer = 0
    }

    private fun tickBuoyant(entity: LivingEntity, cc: ChestCavityInstance) {
        val buoyancy = cc.organScore(CCOrganScores.BUOYANT) - cc.type.getDefaultOrganScore(CCOrganScores.BUOYANT)
        if (buoyancy == 0f) return
        if (entity.onGround()) return
        if (entity.isNoGravity) return
        if (entity is Player && entity.isCreative && entity.abilities.flying) return
        entity.deltaMovement = entity.deltaMovement.add(0.0, buoyancy * 0.02, 0.0)
    }

    private fun tickPhotosynthesis(entity: LivingEntity, cc: ChestCavityInstance) {
        if (entity.level().isClientSide) return
        val photosynthesis = cc.organScore(CCOrganScores.PHOTOSYNTHESIS) - cc.type.getDefaultOrganScore(CCOrganScores.PHOTOSYNTHESIS)
        if (photosynthesis <= 0) return
        cc.photosynthesisProgress += (photosynthesis * entity.level().getMaxLocalRawBrightness(entity.blockPosition())).toInt()
        val threshold = CCConfig.PHOTOSYNTHESIS_FREQUENCY.get() * 8 * 15
        if (cc.photosynthesisProgress < threshold) return
        cc.photosynthesisProgress = 0
        if (entity is Player) entity.foodData.eat(1, 0.5f) else entity.heal(1f)
    }

    private fun tickHydroallergenic(entity: LivingEntity, cc: ChestCavityInstance) {
        if (entity.level().isClientSide) return
        val allergy = cc.organScore(CCOrganScores.HYDROALLERGENIC)
        if (allergy <= 0) return
        if (entity.hasEffect(CCStatusEffects.WATER_VULNERABILITY)) return
        val inWater = entity.isUnderWater || entity.isInWaterRainOrBubble
        if (!inWater) return
        val damage = if (entity.isUnderWater) 10f else 1f
        entity.hurt(entity.damageSources().magic(), damage)
        entity.addEffect(MobEffectInstance(
            CCStatusEffects.WATER_VULNERABILITY,
            (260 / allergy).toInt(), 0, false, false, true
        ))
    }

    private fun tickHydrophobia(entity: LivingEntity, cc: ChestCavityInstance) {
        val hydrophobia = cc.organScore(CCOrganScores.HYDROPHOBIA)
        if (hydrophobia <= 0) return
        if (cc.type.getDefaultOrganScore(CCOrganScores.HYDROPHOBIA) != 0f) return
        if (!entity.isInWaterRainOrBubble) return
        net.tigereye.chestcavity.util.OrganUtil.teleportRandomly(entity, hydrophobia * 32)
    }

    private fun tickCrystalsynthesis(entity: LivingEntity, cc: ChestCavityInstance) {
        if (entity.level().isClientSide) return
        val crystalsynthesis = cc.organScore(CCOrganScores.CRYSTALSYNTHESIS)

        // If connected crystal was destroyed, deal damage
        val crystal = cc.connectedCrystal
        if (crystal != null) {
            if (crystal.isRemoved) {
                entity.hurt(entity.damageSources().starve(), crystalsynthesis * 2)
                cc.connectedCrystal = null
            } else if (crystalsynthesis == 0f) {
                cc.connectedCrystal = null
            }
        }

        if (crystalsynthesis <= 0) return
        if (entity.level().gameTime % CCConfig.CRYSTALSYNTHESIS_FREQUENCY.get() != 0L) return

        // Find nearest end crystal
        val range = CCConfig.CRYSTALSYNTHESIS_RANGE.get().toDouble()
        val box = entity.boundingBox.inflate(range)
        val nearest = entity.level().getEntitiesOfClass(
            net.minecraft.world.entity.boss.enderdragon.EndCrystal::class.java, box
        ).minByOrNull { it.distanceToSqr(entity) }

        val oldCrystal = cc.connectedCrystal
        cc.connectedCrystal = nearest
        if (oldCrystal != null && oldCrystal != nearest) {
            oldCrystal.setBeamTarget(null)
        }

        val linked = cc.connectedCrystal ?: return
        linked.setBeamTarget(entity.blockPosition().below(2))

        // Heal/feed based on crystal link
        if (entity is net.minecraft.world.entity.player.Player) {
            val food = entity.foodData
            when {
                food.needsFood() -> food.eat(1, 0f)
                food.saturationLevel < food.foodLevel -> food.eat(1, crystalsynthesis / 10f)
                else -> entity.heal(crystalsynthesis / 5f)
            }
        } else {
            entity.heal(crystalsynthesis / 5f)
        }
    }

    private fun tickGlowing(entity: LivingEntity, cc: ChestCavityInstance) {
        if (entity.level().isClientSide) return
        if (cc.organScore(CCOrganScores.GLOWING) <= 0) return
        if (entity.hasEffect(MobEffects.GLOWING)) return
        entity.addEffect(MobEffectInstance(MobEffects.GLOWING, 200, 0, false, true, true))
    }

    private fun tickSwimSpeed(entity: LivingEntity, cc: ChestCavityInstance) {
        // Swim speed is applied via the original's ModifyArg on travel() which multiplies
        // the movement input once per frame. Since we can't hook travel() directly, we apply
        // a one-time boost when the entity is swimming and actively moving.
        // This is handled via the movement speed attribute in OrganUpdateListeners instead.
        // No per-tick velocity manipulation needed.
    }

    private fun tickCreeperFuse(entity: LivingEntity, cc: ChestCavityInstance) {
        if (entity !is Creeper) return
        if (!entity.isAlive) return
        if (!cc.opened) return
        if (cc.organScore(CCOrganScores.CREEPY) > 0) return
        // No fuse organ — cancel ignition by setting swell direction to -1
        if (entity.swellDir > 0) {
            entity.swellDir = -1
        }
    }
}
