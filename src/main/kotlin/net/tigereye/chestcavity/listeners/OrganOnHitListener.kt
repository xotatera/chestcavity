package net.tigereye.chestcavity.listeners

import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.tigereye.chestcavity.chestcavities.instance.ChestCavityInstance

fun interface OrganOnHitListener {
    fun onHit(
        source: DamageSource,
        attacker: LivingEntity,
        target: LivingEntity,
        chestCavity: ChestCavityInstance,
        organ: ItemStack,
        damage: Float
    ): Float
}

data class OrganOnHitContext(
    val organ: ItemStack,
    val listener: OrganOnHitListener
)
