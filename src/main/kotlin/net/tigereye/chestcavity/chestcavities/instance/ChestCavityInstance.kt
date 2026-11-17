package net.tigereye.chestcavity.chestcavities.instance

import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.Container
import net.minecraft.world.ContainerListener
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.boss.enderdragon.EndCrystal
import net.tigereye.chestcavity.chestcavities.ChestCavityInventory
import net.tigereye.chestcavity.chestcavities.ChestCavityType
import net.tigereye.chestcavity.listeners.OrganOnHitContext
import java.util.*
import java.util.function.Consumer

class ChestCavityInstance(
    var type: ChestCavityType,
    var owner: LivingEntity
) : ContainerListener {

    var compatibilityId: UUID = owner.uuid
    var opened = false
    val inventory = ChestCavityInventory(instance = this)

    /** True once a full evaluation (scores + attribute modifiers) has been performed at least once. */
    var initialized = false

    /** True once the death transaction for this body has been committed. Guards against a second commit. */
    var deathCommitted = false

    var oldOrganScores: Map<ResourceLocation, Float> = emptyMap()
    var organScores: Map<ResourceLocation, Float>
        get() = _organScores
        set(value) { _organScores = value.toMutableMap() }
    private var _organScores: MutableMap<ResourceLocation, Float> = mutableMapOf()

    val onHitListeners: MutableList<OrganOnHitContext> = mutableListOf()
    val projectileQueue: LinkedList<Consumer<LivingEntity>> = LinkedList()

    var heartBleedTimer = 0
    var bloodPoisonTimer = 0
    var liverTimer = 0
    var metabolismRemainder = 0f
    var lungRemainder = 0f
    var projectileCooldown = 0
    var furnaceProgress = 0
    var photosynthesisProgress = 0
    var connectedCrystal: EndCrystal? = null
    var ccBeingOpened: ChestCavityInstance? = null
    /**
     * Item entities the death transaction shed from this body during the current drops phase. Not
     * persisted: it only tells the later loot listeners which drops are implants, not mob loot.
     */
    var deathDrops: List<net.minecraft.world.entity.item.ItemEntity> = emptyList()

    fun organScore(id: ResourceLocation): Float = _organScores.getOrDefault(id, 0f)
    fun oldOrganScore(id: ResourceLocation): Float = oldOrganScores.getOrDefault(id, 0f)

    override fun containerChanged(container: Container) {
        net.tigereye.chestcavity.util.ChestCavityUtil.evaluateChestCavity(this)
    }

    fun fromTag(tag: CompoundTag, owner: LivingEntity) {
        this.owner = owner
        val ccTag = tag.getCompound("ChestCavity")
        if (ccTag.isEmpty) return

        opened = ccTag.getBoolean("opened")
        heartBleedTimer = ccTag.getInt("HeartTimer")
        bloodPoisonTimer = ccTag.getInt("KidneyTimer")
        liverTimer = ccTag.getInt("LiverTimer")
        metabolismRemainder = ccTag.getFloat("MetabolismRemainder")
        lungRemainder = ccTag.getFloat("LungRemainder")
        furnaceProgress = ccTag.getInt("FurnaceProgress")
        photosynthesisProgress = ccTag.getInt("PhotosynthesisProgress")
        compatibilityId = ccTag.takeIf { it.contains("compatibility_id") }
            ?.getUUID("compatibility_id") ?: owner.uuid

        inventory.removeListener(this)
        if (ccTag.contains("Inventory")) inventory.readTags(ccTag.getList("Inventory", 10))
        inventory.addListener(this)
        // Before the datapack types exist there is nothing meaningful to evaluate; leave the instance
        // uninitialised so the first tick resolves the real type instead of freezing a fallback (L-01).
        if (net.tigereye.chestcavity.chestcavities.types.ChestCavityTypeManager.types.isNotEmpty()) {
            net.tigereye.chestcavity.util.ChestCavityUtil.initializeChestCavity(this)
        }
    }

    fun toTag(tag: CompoundTag) {
        tag.put("ChestCavity", CompoundTag().apply {
            putBoolean("opened", opened)
            putUUID("compatibility_id", compatibilityId)
            putInt("HeartTimer", heartBleedTimer)
            putInt("KidneyTimer", bloodPoisonTimer)
            putInt("LiverTimer", liverTimer)
            putFloat("MetabolismRemainder", metabolismRemainder)
            putFloat("LungRemainder", lungRemainder)
            putInt("FurnaceProgress", furnaceProgress)
            putInt("PhotosynthesisProgress", photosynthesisProgress)
            put("Inventory", inventory.getTags())
        })
    }

    fun copyFrom(other: ChestCavityInstance) {
        opened = other.opened
        type = other.type
        compatibilityId = other.compatibilityId
        heartBleedTimer = other.heartBleedTimer
        liverTimer = other.liverTimer
        bloodPoisonTimer = other.bloodPoisonTimer
        metabolismRemainder = other.metabolismRemainder
        lungRemainder = other.lungRemainder
        furnaceProgress = other.furnaceProgress
        photosynthesisProgress = other.photosynthesisProgress
        // NOTE: connectedCrystal is a live world entity reference and must not travel to another body (L-07).

        inventory.removeListener(this)
        inventory.readTags(other.inventory.getTags())
        inventory.addListener(this)

        // Recalculate for the final inventory/owner/body id (A-08). No network sync here: on a clone
        // the client is being rebuilt and would drop the packet (L-04); the explicit full snapshot is
        // sent once the new player object exists.
        net.tigereye.chestcavity.util.ChestCavityUtil.evaluateChestCavity(this, sync = false)
    }

    /**
     * Releases transient, world-bound state that must not survive this body.
     * Used by death and by body replacement before a clone.
     */
    fun releaseTransientState() {
        projectileQueue.clear()
        projectileCooldown = 0
        onHitListeners.clear()
        connectedCrystal?.setBeamTarget(null)
        connectedCrystal = null
        ccBeingOpened = null
    }
}
