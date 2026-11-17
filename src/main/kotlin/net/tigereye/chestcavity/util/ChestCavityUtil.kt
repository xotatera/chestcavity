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

    fun evaluateChestCavity(cc: ChestCavityInstance, sync: Boolean = true) {
        val scores = cc.organScores.toMutableMap()
        if (!cc.opened) {
            scores.clear()
            cc.type.defaultOrganScores.let { scores.putAll(it) }
            cc.organScores = scores
            cc.initialized = true
            organUpdate(cc, sync)
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
                val compatibility = getCompatibilityLevel(cc, stack)
                if (compatibility < 1) {
                    addOrganScore(CCOrganScores.INCOMPATIBILITY, 1f, scores)
                }
            }
        }

        cc.organScores = scores
        cc.initialized = true
        organUpdate(cc, sync)
    }

    fun organUpdate(cc: ChestCavityInstance, sync: Boolean = true) {
        val changed = cc.oldOrganScores != cc.organScores
        if (changed) {
            OrganUpdateListeners.onOrganUpdate(cc.owner, cc)
            cc.oldOrganScores = cc.organScores.toMap()
        }
        if (!changed || !sync) return
        sendStateToClient(cc)
    }

    /**
     * Pushes the full current state (opened + every organ score) to the owning player if they are
     * a connected server player. Unlike [organUpdate] this ignores whether the score map changed,
     * which is required after login and after an actual respawn has produced a new client entity (L-04).
     */
    fun sendStateToClient(cc: ChestCavityInstance) {
        val owner = cc.owner
        if (owner.level().isClientSide) return
        if (owner !is net.minecraft.server.level.ServerPlayer) return
        if (owner.connection == null) return
        val payload = net.tigereye.chestcavity.registration.ChestCavityUpdatePayload(cc.opened, cc.organScores)
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(owner, payload)
    }

    /**
     * Resolves the datapack-defined type for this owner and performs the first full evaluation.
     * This is the single explicit "initialisation finished" point for a fresh instance (L-01).
     */
    fun initializeChestCavity(cc: ChestCavityInstance, sync: Boolean = false) {
        cc.type = net.tigereye.chestcavity.chestcavities.instance.ChestCavityInstanceFactory.resolveType(cc.owner.type)
        // Before the datapack types are loaded there is nothing to evaluate against; stay
        // uninitialised so a later tick retries instead of freezing an empty score map forever (L-01).
        if (net.tigereye.chestcavity.chestcavities.types.ChestCavityTypeManager.types.isEmpty()) return
        evaluateChestCavity(cc, sync)
    }

    fun openChestCavity(cc: ChestCavityInstance): ChestCavityInventory {
        if (cc.opened) return cc.inventory
        cc.inventory.removeListener(cc)
        cc.opened = true
        // Fills the default inventory, tags it with the body id, then re-evaluates (L-01).
        generateChestCavityIfOpened(cc)
        cc.inventory.addListener(cc)
        return cc.inventory
    }

    fun generateChestCavityIfOpened(cc: ChestCavityInstance, sync: Boolean = true) {
        if (!cc.opened) return
        cc.type.fillChestCavityInventory(cc.inventory)
        cc.type.setOrganCompatibility(cc)
        // Recalculate from the freshly generated inventory so the terminal state is correct (L-01).
        evaluateChestCavity(cc, sync)
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

    /**
     * Commits the death transaction after the death has been approved by the platform.
     *
     * Deliberately not wired to the (cancellable) death event: a later listener may still save the
     * entity, in which case nothing about the body may change (L-03 / A13). Runs in the drops phase
     * instead, where [drops] is the platform's own captured drop collection, so every item leaving the
     * body stays subject to the platform's drop handling and to any listener that cancels or replaces
     * it (L-03).
     */
    fun commitDeath(cc: ChestCavityInstance, drops: MutableCollection<net.minecraft.world.entity.item.ItemEntity>) {
        if (cc.deathCommitted) return
        cc.deathCommitted = true

        // Release transient, world-bound state before anything else (L-07). Done here as well as in
        // the type hook so a type that overrides onDeath without calling back cannot leak them.
        cc.releaseTransientState()
        cc.type.onDeath(cc)
        cc.projectileCooldown = 0
        cc.ccBeingOpened = null

        val owner = cc.owner
        if (owner !is net.minecraft.world.entity.player.Player) {
            // Mobs: only an opened body has organs to shed; the body itself is removed with the entity.
            // Welfare stays player-only, exactly like the original (it lives inside the player branch).
            if (cc.opened) dropUnboundOrgans(cc, drops)
            return
        }

        if (!CCConfig.KEEP_CHEST_CAVITY.get()) {
            // Classify by the OLD body id first, then rebuild the body under a new id (A-01 / L-02).
            if (cc.opened) dropUnboundOrgans(cc, drops)
            val newBodyId = java.util.UUID.randomUUID()
            cc.compatibilityId = newBodyId
            if (cc.opened) {
                // Fills the fresh default inventory against the new body id and re-evaluates. The
                // listener is detached while the slots are rewritten so a half-filled body is never
                // evaluated (and never pushed to the dying client).
                cc.inventory.removeListener(cc)
                try {
                    generateChestCavityIfOpened(cc, sync = false)
                } finally {
                    cc.inventory.addListener(cc)
                }
            }
            // The body id is decided here and must survive a type implementation that still writes it
            // back to the account id; regenerate only tags organs with whatever is current (L-02).
            cc.compatibilityId = newBodyId
            adoptBodyId(cc, owner.uuid, newBodyId)
        }
        // KEEP_CHEST_CAVITY keeps the current body, its inventory and its id untouched (A-09).
        insertWelfareOrgans(cc, drops)
        evaluateChestCavity(cc, sync = false)
    }

    /**
     * Re-tags every organ that a type just stamped with the account id so it belongs to the body id
     * that was decided by the death transaction (L-02).
     *
     * The body of a player is owned by the body, not by the account: two successive lives of the same
     * account are different donors, and an organ that carries the account id would be read as
     * "someone else's organ" (`compatibility == 0`) and shed again on the very next death. This keeps
     * the decision of [commitDeath] authoritative even when the type implementation still writes the
     * account id, and is a no-op once the type no longer writes the id at all.
     */
    private fun adoptBodyId(cc: ChestCavityInstance, accountId: java.util.UUID, bodyId: java.util.UUID) {
        if (accountId == bodyId) return
        val componentType = net.tigereye.chestcavity.registration.CCDataComponents.ORGAN_COMPATIBILITY.get()
        for (i in 0 until cc.inventory.containerSize) {
            val stack = cc.inventory.getItem(i)
            if (stack.isEmpty) continue
            val compat = stack.get(componentType) ?: continue
            if (compat.ownerUuid != accountId) continue
            stack.set(componentType, compat.copy(ownerUuid = bodyId))
        }
        evaluateChestCavity(cc, sync = false)
    }

    /**
     * Drops every organ that is not bound to this body, judged by the body id that was valid before
     * the death (A-01). The stacks are routed into the platform drop collection.
     */
    fun dropUnboundOrgans(cc: ChestCavityInstance, drops: MutableCollection<net.minecraft.world.entity.item.ItemEntity>) {
        cc.inventory.removeListener(cc)
        try {
            for (i in 0 until cc.inventory.containerSize) {
                val stack = cc.inventory.getItem(i)
                if (stack.isEmpty) continue
                if (getCompatibilityLevel(cc, stack) < 2) {
                    collectDrop(cc, cc.inventory.removeItemNoUpdate(i), drops)
                }
            }
        } finally {
            cc.inventory.addListener(cc)
        }
        // The scores must reflect the shed organs before anything (welfare) is decided on them (A-15).
        evaluateChestCavity(cc, sync = false)
    }

    fun insertWelfareOrgans(
        cc: ChestCavityInstance,
        drops: MutableCollection<net.minecraft.world.entity.item.ItemEntity>? = null
    ) {
        if (cc.organScore(CCOrganScores.HEALTH) <= 0) {
            forcefullyAddStack(cc, ItemStack(CCItems.ROTTEN_HEART.get()), 4, drops)
        }
        if (cc.organScore(CCOrganScores.BREATH_RECOVERY) <= 0) {
            forcefullyAddStack(cc, ItemStack(CCItems.ROTTEN_LUNG.get()), 3, drops)
        }
        if (cc.organScore(CCOrganScores.NERVES) <= 0) {
            forcefullyAddStack(cc, ItemStack(CCItems.ROTTEN_SPINE.get()), 13, drops)
        }
        if (cc.organScore(CCOrganScores.STRENGTH) <= 0) {
            forcefullyAddStack(cc, ItemStack(net.minecraft.world.item.Items.ROTTEN_FLESH, 16), 0, drops)
        }
    }

    /**
     * Adds a welfare organ without ever overwriting an existing stack (A-10).
     *
     * Order matches the original: merge into partial stacks / use an empty slot first; only when the
     * cavity cannot take the stack at all is the preferred slot's occupant removed, and that occupant
     * is preserved by routing it into the drop collection. Players with the vanilla keepInventory
     * gamerule get a main-inventory attempt before any body slot is disturbed. Anything that still
     * does not fit is dropped rather than silently discarded.
     */
    private fun forcefullyAddStack(
        cc: ChestCavityInstance,
        stack: ItemStack,
        slot: Int,
        drops: MutableCollection<net.minecraft.world.entity.item.ItemEntity>?
    ) {
        if (stack.isEmpty) return
        var remainder = cc.inventory.addItem(stack)

        if (!remainder.isEmpty) {
            val owner = cc.owner
            if (owner is net.minecraft.world.entity.player.Player &&
                owner.level().gameRules.getBoolean(net.minecraft.world.level.GameRules.RULE_KEEPINVENTORY)
            ) {
                // Vanilla keeps the main inventory: let it absorb the surplus before touching the body.
                if (owner.inventory.add(remainder)) return
            }
        }

        if (!remainder.isEmpty && slot in 0 until cc.inventory.containerSize) {
            // The cavity is full: evict only the preferred slot, preserving what was there.
            val evicted = cc.inventory.removeItemNoUpdate(slot)
            if (!evicted.isEmpty) collectDrop(cc, evicted, drops)
            remainder = cc.inventory.addItem(remainder)
        }

        if (!remainder.isEmpty) collectDrop(cc, remainder, drops)
    }

    /** Routes an item out of the body: into the platform drop collection when there is one (L-03). */
    private fun collectDrop(
        cc: ChestCavityInstance,
        stack: ItemStack,
        drops: MutableCollection<net.minecraft.world.entity.item.ItemEntity>?
    ) {
        if (stack.isEmpty) return
        if (drops == null) {
            cc.owner.spawnAtLocation(stack)
            return
        }
        val itemEntity = net.minecraft.world.entity.item.ItemEntity(
            cc.owner.level(), cc.owner.x, cc.owner.y, cc.owner.z, stack
        )
        itemEntity.setDefaultPickUpDelay()
        drops.add(itemEntity)
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

    fun getCompatibilityLevel(cc: ChestCavityInstance, stack: ItemStack): Int {
        if (stack.isEmpty) return 1
        val compat = stack.get(net.tigereye.chestcavity.registration.CCDataComponents.ORGAN_COMPATIBILITY.get())
        return when {
            compat == null -> 1  // Untagged organ — universal
            compat.ownerUuid == cc.compatibilityId -> 2  // Your organ
            else -> 0  // Someone else's organ — incompatible
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
