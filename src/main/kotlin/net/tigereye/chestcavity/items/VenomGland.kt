package net.tigereye.chestcavity.items

import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.LlamaSpit
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.tigereye.chestcavity.CCConfig
import net.tigereye.chestcavity.chestcavities.instance.ChestCavityInstance
import net.tigereye.chestcavity.listeners.OrganOnHitListener
import net.tigereye.chestcavity.registration.CCStatusEffects

class VenomGland(properties: Properties) : Item(properties), OrganOnHitListener {

    override fun onHit(
        source: DamageSource,
        attacker: LivingEntity,
        target: LivingEntity,
        chestCavity: ChestCavityInstance,
        organ: ItemStack,
        damage: Float
    ): Float {
        if (attacker.getItemInHand(attacker.usedItemHand).isEmpty) return damage

        val projectile = source.directEntity
        if (projectile != null && projectile !is LlamaSpit) return damage

        val cooldownDuration = CCConfig.VENOM_COOLDOWN.get()
        val existing = attacker.getEffect(CCStatusEffects.VENOM_COOLDOWN)
        if (existing != null && existing.duration != cooldownDuration) return damage

        val potionContents = organ.get(net.minecraft.core.component.DataComponents.POTION_CONTENTS)
        val customEffects = potionContents?.allEffects?.toList()
        if (!customEffects.isNullOrEmpty()) {
            customEffects.forEach { effect -> target.addEffect(MobEffectInstance(effect)) }
        } else {
            target.addEffect(MobEffectInstance(MobEffects.POISON, 200, 0))
        }

        attacker.addEffect(MobEffectInstance(
            CCStatusEffects.VENOM_COOLDOWN, cooldownDuration, 0
        ))
        if (attacker is Player) attacker.causeFoodExhaustion(0.1f)
        return damage
    }
}
