package net.tigereye.chestcavity.util

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.tigereye.chestcavity.CCConfig
import net.tigereye.chestcavity.ChestCavity
import net.tigereye.chestcavity.chestcavities.ChestCavityEntity
import net.tigereye.chestcavity.chestcavities.ChestCavityInventory
import net.tigereye.chestcavity.chestcavities.instance.ChestCavityInstance
import net.tigereye.chestcavity.chestcavities.organs.OrganManager
import net.tigereye.chestcavity.listeners.OrganOnHitContext
import net.tigereye.chestcavity.listeners.OrganOnHitListener
import net.tigereye.chestcavity.listeners.OrganTickListeners
import net.tigereye.chestcavity.listeners.OrganUpdateListeners
import net.tigereye.chestcavity.registration.CCItems
import net.tigereye.chestcavity.registration.CCOrganScores
import net.tigereye.chestcavity.registration.CCTagOrgans
import net.tigereye.chestcavity.registration.CCStatusEffects
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

object ChestCavityUtil {

    fun addOrganScore(id: ResourceLocation, value: Float, scores: MutableMap<ResourceLocation, Float>) {
        scores[id] = scores.getOrDefault(id, 0f) + value
    }

    // --- Defense pipeline ---

    fun applyDefenses(cc: ChestCavityInstance, source: DamageSource, damage: Float): Float {
        if (!cc.opened) return damage

        var result = damage
        if (!source.`is`(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR)) result = applyBoneDefense(cc, result)
        // Fire/impact/leaping defenses will be wired up when damage types are fully ported
        return result
    }

    fun applyBoneDefense(cc: ChestCavityInstance, damage: Float): Float {
        val boneDiff = (cc.organScore(CCOrganScores.DEFENSE) - cc.type.getDefaultOrganScore(CCOrganScores.DEFENSE)) / 4
        return damage * (1.0 - CCConfig.BONE_DEFENSE.get()).pow(boneDiff.toDouble()).toFloat()
    }

    fun applyFireResistant(cc: ChestCavityInstance, damage: Float): Float {
        val fireproof = cc.organScore(CCOrganScores.FIRE_RESISTANT)
        if (fireproof <= 0) return damage
        return damage * (1.0 - CCConfig.FIREPROOF_DEFENSE.get()).pow((fireproof / 4).toDouble()).toFloat()
    }

    fun applyImpactResistant(cc: ChestCavityInstance, damage: Float): Float {
        val impact = cc.organScore(CCOrganScores.IMPACT_RESISTANT)
        if (impact <= 0) return damage
        return damage * (1.0 - CCConfig.IMPACT_DEFENSE.get()).pow((impact / 4).toDouble()).toFloat()
    }

    fun applyLeaping(cc: ChestCavityInstance, velocity: Float): Float {
        val diff = cc.organScore(CCOrganScores.LEAPING) - cc.type.getDefaultOrganScore(CCOrganScores.LEAPING)
        return velocity * max(0f, 1f + diff * 0.25f)
    }

    fun applyLeapingToFallDamage(cc: ChestCavityInstance, damage: Float): Float {
        val diff = cc.organScore(CCOrganScores.LEAPING) - cc.type.getDefaultOrganScore(CCOrganScores.LEAPING)
        if (diff <= 0) return damage
        return max(0f, damage - diff * diff / 4)
    }

    // --- Speed modifiers ---

    fun applySwimSpeedInWater(cc: ChestCavityInstance): Float {
        if (!cc.opened) return 1f
        if (!cc.owner.isInWater) return 1f
        val diff = cc.organScore(CCOrganScores.SWIM_SPEED) - cc.type.getDefaultOrganScore(CCOrganScores.SWIM_SPEED)
        if (diff == 0f) return 1f
        return max(0f, 1f + diff * CCConfig.SWIMSPEED_FACTOR.get().toFloat() / 8f)
    }

    fun applyNervesToMining(cc: ChestCavityInstance, miningSpeed: Float): Float {
        val defaultNerves = cc.type.getDefaultOrganScore(CCOrganScores.NERVES)
        if (defaultNerves == 0f) return miningSpeed
        val diff = cc.organScore(CCOrganScores.NERVES) - defaultNerves
        return miningSpeed * (1f + CCConfig.NERVES_HASTE.get().toFloat() * diff)
    }

    // --- Digestion ---

    fun applyDigestion(cc: ChestCavityInstance, digestion: Float, hunger: Int): Int {
        if (digestion == 1f) return hunger
        if (digestion < 0) {
            cc.owner.addEffect(MobEffectInstance(MobEffects.CONFUSION, (-hunger * digestion * 400).toInt()))
            return 0
        }
        return max((hunger * digestion).toInt(), 1)
    }

    fun applyNutrition(cc: ChestCavityInstance, nutrition: Float, saturation: Float): Float {
        if (nutrition == 4f) return saturation
        if (nutrition < 0) {
            cc.owner.addEffect(MobEffectInstance(MobEffects.HUNGER, (saturation * nutrition * 800).toInt()))
            return 0f
        }
        return saturation * nutrition / 4f
    }

    fun applySpleenMetabolism(cc: ChestCavityInstance, foodStarvationTimer: Int): Int {
        if (!cc.opened) return foodStarvationTimer
        val diff = cc.organScore(CCOrganScores.METABOLISM) - cc.type.getDefaultOrganScore(CCOrganScores.METABOLISM)
        if (diff == 0f) return foodStarvationTimer

        var result = foodStarvationTimer
        if (diff > 0) {
            cc.metabolismRemainder += diff
            result += cc.metabolismRemainder.toInt()
        } else {
            cc.metabolismRemainder += 1f - 1f / (-diff + 1f)
            result -= cc.metabolismRemainder.toInt()
        }
        cc.metabolismRemainder %= 1f
        return result
    }

    // --- Chest cavity lifecycle ---

    fun evaluateChestCavity(cc: ChestCavityInstance) {
        val scores = cc.organScores.toMutableMap()
        if (!cc.opened) {
            scores.clear()
            cc.type.defaultOrganScores.let { scores.putAll(it) }
            cc.organScores = scores
            organUpdate(cc)
            return
        }

        cc.onHitListeners.clear()
        cc.type.loadBaseOrganScores(scores)

        for (i in 0 until cc.inventory.containerSize) {
            val stack = cc.inventory.getItem(i)
            if (stack.isEmpty) continue

            // Shulker boxes act as pseudo-organs: contents contribute at 1/27 strength
            if (isShulkerBox(stack)) {
                evaluateShulkerContents(stack, cc, scores)
                continue
            }

            val data = lookupOrgan(stack, cc) ?: continue
            val ratio = min(stack.count.toFloat() / stack.maxStackSize.toFloat(), 1f)
            data.organScores.forEach { (key, value) ->
                addOrganScore(key, value * ratio, scores)
            }
            if (stack.item is OrganOnHitListener) {
                cc.onHitListeners.add(OrganOnHitContext(stack, stack.item as OrganOnHitListener))
            }
            if (!data.pseudoOrgan) {
                // TODO: compatibility checking when enchantment system is ported
            }
        }

        cc.organScores = scores
        organUpdate(cc)
    }

    fun organUpdate(cc: ChestCavityInstance) {
        if (cc.oldOrganScores == cc.organScores) return
        OrganUpdateListeners.onOrganUpdate(cc.owner, cc)
        cc.oldOrganScores = cc.organScores.toMap()
        // Sync to client (only if player has an active connection)
        val owner = cc.owner
        if (!owner.level().isClientSide && owner is net.minecraft.server.level.ServerPlayer && owner.connection != null) {
            val payload = net.tigereye.chestcavity.registration.ChestCavityUpdatePayload(cc.opened, cc.organScores)
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(owner, payload)
        }
    }

    fun openChestCavity(cc: ChestCavityInstance): ChestCavityInventory {
        if (cc.opened) return cc.inventory
        cc.inventory.removeListener(cc)
        cc.opened = true
        generateChestCavityIfOpened(cc)
        cc.inventory.addListener(cc)
        return cc.inventory
    }

    fun generateChestCavityIfOpened(cc: ChestCavityInstance) {
        if (!cc.opened) return
        cc.type.fillChestCavityInventory(cc.inventory)
        cc.type.setOrganCompatibility(cc)
    }

    // --- On-hit ---

    fun onHit(cc: ChestCavityInstance, source: DamageSource, target: LivingEntity, damage: Float): Float {
        if (!cc.opened) return damage
        val result = cc.onHitListeners.fold(damage) { dmg, ctx ->
            ctx.listener.onHit(source, cc.owner, target, cc, ctx.organ, dmg)
        }
        organUpdate(cc)
        return result
    }

    // --- Tick ---

    fun onTick(cc: ChestCavityInstance) {
        if (cc.opened) {
            OrganTickListeners.tickAll(cc.owner, cc)
        }
        organUpdate(cc)
    }

    // --- Death ---

    fun onDeath(entity: ChestCavityEntity) {
        val cc = entity.chestCavityInstance
        cc.type.onDeath(cc)
        val owner = cc.owner
        if (owner is net.minecraft.world.entity.player.Player) {
            if (!CCConfig.KEEP_CHEST_CAVITY.get()) {
                cc.compatibilityId = java.util.UUID.randomUUID()
                generateChestCavityIfOpened(cc)
            }
            insertWelfareOrgans(cc)
        }
    }

    fun insertWelfareOrgans(cc: ChestCavityInstance) {
        if (cc.organScore(CCOrganScores.HEALTH) <= 0) {
            forcefullyAddStack(cc, ItemStack(CCItems.ROTTEN_HEART.get()), 4)
        }
        if (cc.organScore(CCOrganScores.BREATH_RECOVERY) <= 0) {
            forcefullyAddStack(cc, ItemStack(CCItems.ROTTEN_LUNG.get()), 3)
        }
        if (cc.organScore(CCOrganScores.NERVES) <= 0) {
            forcefullyAddStack(cc, ItemStack(CCItems.ROTTEN_SPINE.get()), 13)
        }
        if (cc.organScore(CCOrganScores.STRENGTH) <= 0) {
            forcefullyAddStack(cc, ItemStack(net.minecraft.world.item.Items.ROTTEN_FLESH, 16), 0)
        }
    }

    private fun forcefullyAddStack(cc: ChestCavityInstance, stack: ItemStack, slot: Int) {
        if (!cc.inventory.canAddItem(stack)) {
            cc.owner.spawnAtLocation(cc.inventory.removeItemNoUpdate(slot))
        }
        cc.inventory.setItem(slot, stack)
    }

    private const val SHULKER_DILUTION = 1f / 27f

    private fun isShulkerBox(stack: ItemStack): Boolean {
        val item = stack.item
        if (item !is net.minecraft.world.item.BlockItem) return false
        return item.block is net.minecraft.world.level.block.ShulkerBoxBlock
    }

    private fun evaluateShulkerContents(
        stack: ItemStack,
        cc: ChestCavityInstance,
        scores: MutableMap<ResourceLocation, Float>
    ) {
        val container = stack.get(net.minecraft.core.component.DataComponents.CONTAINER) ?: return
        container.stream().forEach { innerStack ->
            if (innerStack.isEmpty) return@forEach
            val data = lookupOrgan(innerStack, cc) ?: return@forEach
            val ratio = min(innerStack.count.toFloat() / innerStack.maxStackSize.toFloat(), 1f)
            data.organScores.forEach { (key, value) ->
                addOrganScore(key, value * ratio * SHULKER_DILUTION, scores)
            }
        }
    }

    fun destroyOrgansWithKey(cc: ChestCavityInstance, organ: ResourceLocation) {
        for (i in 0 until cc.inventory.containerSize) {
            val stack = cc.inventory.getItem(i)
            if (stack.isEmpty) continue
            val data = lookupOrgan(stack, cc) ?: continue
            if (organ !in data.organScores) continue
            cc.inventory.removeItemNoUpdate(i)
        }
        cc.inventory.setChanged()
    }

    // --- Organ lookup ---

    fun lookupOrgan(stack: ItemStack, cc: ChestCavityInstance): net.tigereye.chestcavity.chestcavities.organs.OrganData? {
        cc.type.catchExceptionalOrgan(stack)?.let { return it }
        OrganManager.readNbtOrganData(stack)?.let { return it }
        OrganManager.getEntry(stack)?.let { return it }
        // Check tag-based organs
        for ((tag, scores) in CCTagOrgans.tagMap) {
            if (stack.`is`(tag)) {
                return net.tigereye.chestcavity.chestcavities.organs.OrganData(pseudoOrgan = true, organScores = scores)
            }
        }
        return null
    }

    // --- Debug ---

    fun outputOrganScores(cc: ChestCavityInstance, output: (String) -> Unit) {
        val name = runCatching { cc.owner.displayName?.string }.getOrDefault("Unknown")
        output("[Chest Cavity] Displaying ${name}'s organ scores:")
        cc.organScores.forEach { (key, value) -> output("${key.path}: $value") }
    }
}
