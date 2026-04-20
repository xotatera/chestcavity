package net.tigereye.chestcavity.chestcavities.instance

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.tigereye.chestcavity.chestcavities.ChestCavityType
import net.tigereye.chestcavity.chestcavities.types.ChestCavityTypeManager
import net.tigereye.chestcavity.chestcavities.types.GeneratedChestCavityType

object ChestCavityInstanceFactory {

    private val DEFAULT_TYPE = GeneratedChestCavityType()

    fun create(entityType: EntityType<*>, owner: LivingEntity): ChestCavityInstance {
        val type = resolveType(entityType)
        return ChestCavityInstance(type, owner)
    }

    fun resolveType(entityType: EntityType<*>): ChestCavityType {
        val entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entityType)
        val typeId = ChestCavityTypeManager.assignments[entityId] ?: return DEFAULT_TYPE
        return ChestCavityTypeManager.types[typeId] ?: DEFAULT_TYPE
    }
}
