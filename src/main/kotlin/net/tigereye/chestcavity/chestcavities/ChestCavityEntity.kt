package net.tigereye.chestcavity.chestcavities

import net.minecraft.world.entity.Entity
import net.tigereye.chestcavity.chestcavities.instance.ChestCavityInstance

interface ChestCavityEntity {
    var chestCavityInstance: ChestCavityInstance

    companion object {
        fun of(entity: Entity?): ChestCavityEntity? =
            entity as? ChestCavityEntity
    }
}
