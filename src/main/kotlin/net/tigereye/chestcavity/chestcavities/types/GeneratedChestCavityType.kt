package net.tigereye.chestcavity.chestcavities.types

import net.minecraft.resources.ResourceLocation
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.tigereye.chestcavity.CCConfig
import net.tigereye.chestcavity.chestcavities.ChestCavityInventory
import net.tigereye.chestcavity.chestcavities.ChestCavityType
import net.tigereye.chestcavity.chestcavities.instance.ChestCavityInstance
import net.tigereye.chestcavity.chestcavities.organs.OrganData

class GeneratedChestCavityType(
    private var defaultInventory: ChestCavityInventory = ChestCavityInventory(),
    private var baseOrganScores: Map<ResourceLocation, Float> = emptyMap(),
    private var exceptionalOrgans: Map<Ingredient, Map<ResourceLocation, Float>> = emptyMap(),
    private var forbiddenSlots: List<Int> = emptyList(),
    var dropRateMultiplier: Float = 1f,
    var playerChestCavity: Boolean = false,
    var bossChestCavity: Boolean = false,
) : ChestCavityType {

    override val defaultOrganScores: Map<ResourceLocation, Float>
        get() = _cachedDefaults ?: computeDefaultOrganScores().also { _cachedDefaults = it }
    private var _cachedDefaults: Map<ResourceLocation, Float>? = null

    private fun computeDefaultOrganScores(): Map<ResourceLocation, Float> {
        val scores = baseOrganScores.toMutableMap()
        val organManager = net.tigereye.chestcavity.chestcavities.organs.OrganManager
        for (i in 0 until defaultInventory.containerSize) {
            val stack = defaultInventory.getItem(i)
            if (stack.isEmpty) continue
            val data = organManager.getEntry(stack)
                ?: catchExceptionalOrgan(stack)
                ?: continue
            val ratio = kotlin.math.min(stack.count.toFloat() / stack.maxStackSize.toFloat(), 1f)
            data.organScores.forEach { (key, value) ->
                scores[key] = scores.getOrDefault(key, 0f) + value * ratio
            }
        }
        return scores
    }

    override val heartBleedCap: Float
        get() = if (bossChestCavity) 300f else 4f

    override fun getDefaultChestCavity(): ChestCavityInventory = defaultInventory

    override fun isSlotForbidden(index: Int): Boolean = index in forbiddenSlots

    override fun fillChestCavityInventory(chestCavity: ChestCavityInventory) {
        for (i in 0 until defaultInventory.containerSize.coerceAtMost(chestCavity.containerSize)) {
            chestCavity.setItem(i, defaultInventory.getItem(i).copy())
        }
    }

    override fun loadBaseOrganScores(organScores: MutableMap<ResourceLocation, Float>) {
        organScores.clear()
        organScores.putAll(baseOrganScores)
    }

    override fun catchExceptionalOrgan(slot: ItemStack): OrganData? {
        for ((ingredient, scores) in exceptionalOrgans) {
            if (!ingredient.test(slot)) continue
            return OrganData(pseudoOrgan = true, organScores = scores)
        }
        return null
    }

    override fun generateLootDrops(random: RandomSource, looting: Int): List<ItemStack> {
        if (playerChestCavity) return emptyList()

        val organPool = (0 until defaultInventory.containerSize)
            .map { defaultInventory.getItem(it) }
            .filter { !it.isEmpty }
            .map { it.copy() }
            .toMutableList()

        if (organPool.isEmpty()) return emptyList()

        val drops = mutableListOf<ItemStack>()
        if (bossChestCavity) {
            val rolls = 3 + random.nextInt(2 + looting) + random.nextInt(2 + looting)
            drawOrgansFromPile(organPool, rolls, random, drops)
        } else {
            val chance = net.tigereye.chestcavity.CCConfig.UNIVERSAL_DONOR_RATE.get().toFloat() +
                net.tigereye.chestcavity.CCConfig.ORGAN_BUNDLE_LOOTING_BOOST.get().toFloat() * looting
            if (random.nextFloat() < chance) {
                val rolls = 1 + random.nextInt(3) + random.nextInt(3)
                drawOrgansFromPile(organPool, rolls, random, drops)
            }
        }
        return drops
    }

    private fun drawOrgansFromPile(pile: MutableList<ItemStack>, rolls: Int, random: RandomSource, loot: MutableList<ItemStack>) {
        repeat(rolls) {
            if (pile.isEmpty()) return
            val index = random.nextInt(pile.size)
            val rolled = pile.removeAt(index).copy()
            if (rolled.count > 1) rolled.count = 1 + random.nextInt(rolled.maxStackSize)
            loot.add(rolled)
        }
    }

    override fun setOrganCompatibility(instance: ChestCavityInstance) {
        instance.compatibilityId = instance.owner.uuid
    }

    override fun isOpenable(instance: ChestCavityInstance): Boolean {
        if (playerChestCavity) return true
        val owner = instance.owner
        val chestVulnerable = owner.getItemBySlot(EquipmentSlot.CHEST).isEmpty
        if (!chestVulnerable) return false
        val easeOfAccess = instance.organScore(net.tigereye.chestcavity.registration.CCOrganScores.EASE_OF_ACCESS) > 0
        if (easeOfAccess) return true
        val health = owner.health
        val maxHealth = owner.maxHealth
        val absThreshold = CCConfig.CHEST_OPENER_ABSOLUTE_HEALTH_THRESHOLD.get()
        val fracThreshold = CCConfig.CHEST_OPENER_FRACTIONAL_HEALTH_THRESHOLD.get().toFloat()
        return health <= absThreshold || health <= maxHealth * fracThreshold
    }

    override fun onDeath(instance: ChestCavityInstance) {
        // Default: no special behavior on death
    }

    // --- Mutators for serializer ---

    fun invalidateCache() { _cachedDefaults = null }
    fun setDefaultInventory(inv: ChestCavityInventory) { defaultInventory = inv }
    fun setBaseOrganScores(scores: Map<ResourceLocation, Float>) { baseOrganScores = scores; _cachedDefaults = null }
    fun setExceptionalOrgans(organs: Map<Ingredient, Map<ResourceLocation, Float>>) { exceptionalOrgans = organs }
    fun setForbiddenSlots(slots: List<Int>) { forbiddenSlots = slots }
}
