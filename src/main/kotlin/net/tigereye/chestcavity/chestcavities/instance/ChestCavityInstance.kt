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
        connectedCrystal = other.connectedCrystal

        inventory.removeListener(this)
        inventory.readTags(other.inventory.getTags())
        inventory.addListener(this)
    }
}
