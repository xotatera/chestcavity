package net.tigereye.chestcavity.listeners

import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.block.Blocks
import net.tigereye.chestcavity.CCConfig
import net.tigereye.chestcavity.chestcavities.instance.ChestCavityInstance
import net.tigereye.chestcavity.registration.CCOrganScores
import net.tigereye.chestcavity.registration.CCStatusEffects
import net.tigereye.chestcavity.util.ChestCavityUtil
import net.tigereye.chestcavity.util.OrganUtil

typealias AbilityHandler = (LivingEntity, ChestCavityInstance) -> Unit

object OrganActivationListeners {

    private val handlers: Map<ResourceLocation, AbilityHandler> = mapOf(
        CCOrganScores.CREEPY to ::activateCreepy,
        CCOrganScores.DRAGON_BREATH to ::activateDragonBreath,
        CCOrganScores.DRAGON_BOMBS to ::activateDragonBombs,
        CCOrganScores.FORCEFUL_SPIT to ::activateForcefulSpit,
        CCOrganScores.FURNACE_POWERED to ::activateFurnacePowered,
        CCOrganScores.IRON_REPAIR to ::activateIronRepair,
        CCOrganScores.PYROMANCY to ::activatePyromancy,
        CCOrganScores.GHASTLY to ::activateGhastly,
        CCOrganScores.GRAZING to ::activateGrazing,
        CCOrganScores.SHULKER_BULLETS to ::activateShulkerBullets,
        CCOrganScores.SILK to ::activateSilk,
    )

    fun activate(id: ResourceLocation, cc: ChestCavityInstance): Boolean {
        val handler = handlers[id] ?: return false
        handler(cc.owner, cc)
        return true
    }

    private fun activateCreepy(entity: LivingEntity, cc: ChestCavityInstance) {
        if (cc.organScore(CCOrganScores.CREEPY) < 1) return
        if (entity.hasEffect(CCStatusEffects.EXPLOSION_COOLDOWN)) return
        val yield = cc.organScore(CCOrganScores.EXPLOSIVE)
        ChestCavityUtil.destroyOrgansWithKey(cc, CCOrganScores.EXPLOSIVE)
        OrganUtil.explode(entity, yield)
        if (!entity.isAlive) return
        entity.addEffect(MobEffectInstance(
            CCStatusEffects.EXPLOSION_COOLDOWN, CCConfig.EXPLOSION_COOLDOWN.get(), 0, false, false, true
        ))
    }

    private fun activateDragonBreath(entity: LivingEntity, cc: ChestCavityInstance) {
        val breath = cc.organScore(CCOrganScores.DRAGON_BREATH)
        if (breath <= 0) return
        if (entity is Player) entity.causeFoodExhaustion(breath * 0.6f)
        if (entity.hasEffect(CCStatusEffects.DRAGON_BREATH_COOLDOWN)) return
        entity.addEffect(MobEffectInstance(
            CCStatusEffects.DRAGON_BREATH_COOLDOWN, CCConfig.DRAGON_BREATH_COOLDOWN.get(), 0, false, false, true
        ))
        cc.projectileQueue.add(OrganUtil::spawnDragonBreath)
    }

    private fun activateDragonBombs(entity: LivingEntity, cc: ChestCavityInstance) {
        if (cc.organScore(CCOrganScores.DRAGON_BOMBS) < 1) return
        if (entity.hasEffect(CCStatusEffects.DRAGON_BOMB_COOLDOWN)) return
        OrganUtil.queueDragonBombs(entity, cc, cc.organScore(CCOrganScores.DRAGON_BOMBS).toInt())
    }

    private fun activateForcefulSpit(entity: LivingEntity, cc: ChestCavityInstance) {
        if (cc.organScore(CCOrganScores.FORCEFUL_SPIT) < 1) return
        if (entity.hasEffect(CCStatusEffects.FORCEFUL_SPIT_COOLDOWN)) return
        OrganUtil.queueForcefulSpit(entity, cc, cc.organScore(CCOrganScores.FORCEFUL_SPIT).toInt())
    }

    private fun activateFurnacePowered(entity: LivingEntity, cc: ChestCavityInstance) {
        val furnacePowered = cc.organScore(CCOrganScores.FURNACE_POWERED).toInt()
        if (furnacePowered < 1) return

        val mainHand = entity.getItemBySlot(EquipmentSlot.MAINHAND)
        val offHand = entity.getItemBySlot(EquipmentSlot.OFFHAND)

        val fuelMain = mainHand.getBurnTime(null)
        val fuelOff = offHand.getBurnTime(null)

        val stack = when {
            fuelMain > 0 -> mainHand
            fuelOff > 0 -> offHand
            else -> return
        }
        val fuelValue = if (fuelMain > 0) fuelMain else fuelOff

        val currentAmplifier = entity.getEffect(CCStatusEffects.FURNACE_POWER)?.amplifier ?: -1
        if (currentAmplifier >= furnacePowered - 1) return

        entity.removeEffect(CCStatusEffects.FURNACE_POWER)
        entity.addEffect(MobEffectInstance(
            CCStatusEffects.FURNACE_POWER, fuelValue, currentAmplifier + 1, false, false, true
        ))
        stack.shrink(1)
    }

    private fun activateIronRepair(entity: LivingEntity, cc: ChestCavityInstance) {
        val ironRepair = cc.organScore(CCOrganScores.IRON_REPAIR) - cc.type.getDefaultOrganScore(CCOrganScores.IRON_REPAIR)
        if (ironRepair <= 0) return
        if (entity.hasEffect(CCStatusEffects.IRON_REPAIR_COOLDOWN)) return
        if (entity.health >= entity.maxHealth) return

        val ironTag = net.minecraft.tags.ItemTags.create(
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(net.tigereye.chestcavity.ChestCavity.MODID, "iron_repair_material")
        )
        val stack = entity.getItemBySlot(EquipmentSlot.MAINHAND).takeIf { !it.isEmpty && it.`is`(ironTag) }
            ?: entity.getItemBySlot(EquipmentSlot.OFFHAND).takeIf { !it.isEmpty && it.`is`(ironTag) }
            ?: return

        entity.heal(entity.maxHealth * CCConfig.IRON_REPAIR_PERCENT.get().toFloat())
        entity.playSound(SoundEvents.IRON_GOLEM_REPAIR, 0.75f, 1f)
        entity.addEffect(MobEffectInstance(
            CCStatusEffects.IRON_REPAIR_COOLDOWN,
            (CCConfig.IRON_REPAIR_COOLDOWN.get() / ironRepair).toInt(),
            0, false, false, true
        ))
        stack.shrink(1)
    }

    private fun activateGhastly(entity: LivingEntity, cc: ChestCavityInstance) {
        if (cc.organScore(CCOrganScores.GHASTLY) < 1) return
        if (entity.hasEffect(CCStatusEffects.GHASTLY_COOLDOWN)) return
        OrganUtil.queueGhastlyFireballs(entity, cc, cc.organScore(CCOrganScores.GHASTLY).toInt())
    }

    private fun activateGrazing(entity: LivingEntity, cc: ChestCavityInstance) {
        val grazing = cc.organScore(CCOrganScores.GRAZING)
        if (grazing <= 0) return

        val blockPos = entity.blockPosition().below()
        val level = entity.level()
        val state = level.getBlockState(blockPos)

        val replacement = when {
            state.`is`(Blocks.GRASS_BLOCK) || state.`is`(Blocks.MYCELIUM) -> Blocks.DIRT.defaultBlockState()
            state.`is`(Blocks.CRIMSON_NYLIUM) || state.`is`(Blocks.WARPED_NYLIUM) -> Blocks.NETHERRACK.defaultBlockState()
            else -> return
        }
        level.setBlock(blockPos, replacement, 2)

        val baseDuration = CCConfig.RUMINATION_TIME.get() * CCConfig.RUMINATION_GRASS_PER_SQUARE.get()
        val maxDuration = baseDuration * CCConfig.RUMINATION_SQUARES_PER_STOMACH.get() * grazing.toInt()
        val currentDuration = entity.getEffect(CCStatusEffects.RUMINATING)?.duration ?: 0
        val newDuration = (currentDuration + baseDuration).coerceAtMost(maxDuration)

        entity.addEffect(MobEffectInstance(
            CCStatusEffects.RUMINATING, newDuration, 0, false, false, true
        ))
    }

    private fun activatePyromancy(entity: LivingEntity, cc: ChestCavityInstance) {
        if (cc.organScore(CCOrganScores.PYROMANCY) < 1) return
        if (entity.hasEffect(CCStatusEffects.PYROMANCY_COOLDOWN)) return
        OrganUtil.queuePyromancyFireballs(entity, cc, cc.organScore(CCOrganScores.PYROMANCY).toInt())
    }

    private fun activateShulkerBullets(entity: LivingEntity, cc: ChestCavityInstance) {
        if (cc.organScore(CCOrganScores.SHULKER_BULLETS) < 1) return
        if (entity.hasEffect(CCStatusEffects.SHULKER_BULLET_COOLDOWN)) return
        OrganUtil.queueShulkerBullets(entity, cc, cc.organScore(CCOrganScores.SHULKER_BULLETS).toInt())
    }

    private fun activateSilk(entity: LivingEntity, cc: ChestCavityInstance) {
        if (cc.organScore(CCOrganScores.SILK) == 0f) return
        if (entity.hasEffect(CCStatusEffects.SILK_COOLDOWN)) return
        if (!OrganUtil.spinWeb(entity, cc, cc.organScore(CCOrganScores.SILK))) return
        entity.addEffect(MobEffectInstance(
            CCStatusEffects.SILK_COOLDOWN, CCConfig.SILK_COOLDOWN.get(), 0, false, false, true
        ))
    }
}
