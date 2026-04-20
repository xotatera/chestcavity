package net.tigereye.chestcavity.registration

import net.minecraft.core.registries.Registries
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectCategory
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import net.tigereye.chestcavity.ChestCavity

object CCStatusEffects {
    val MOB_EFFECTS: DeferredRegister<MobEffect> =
        DeferredRegister.create(Registries.MOB_EFFECT, ChestCavity.MODID)

    val ORGAN_REJECTION = effect("organ_rejection", MobEffectCategory.HARMFUL, 0xC8FF00)
    val ARROW_DODGE_COOLDOWN = cooldown("arrow_dodge_cooldown")
    val DRAGON_BOMB_COOLDOWN = cooldown("dragon_bomb_cooldown")
    val DRAGON_BREATH_COOLDOWN = cooldown("dragon_breath_cooldown")
    val EXPLOSION_COOLDOWN = cooldown("explosion_cooldown")
    val FORCEFUL_SPIT_COOLDOWN = cooldown("forceful_spit_cooldown")
    val FURNACE_POWER = effect("furnace_power", MobEffectCategory.BENEFICIAL, 0xC8FF00)
    val GHASTLY_COOLDOWN = cooldown("ghastly_cooldown")
    val IRON_REPAIR_COOLDOWN = cooldown("iron_repair_cooldown")
    val PYROMANCY_COOLDOWN = cooldown("pyromancy_cooldown")
    val RUMINATING = effect("ruminating", MobEffectCategory.BENEFICIAL, 0xC8FF00)
    val SHULKER_BULLET_COOLDOWN = cooldown("shulker_bullet_cooldown")
    val SILK_COOLDOWN = cooldown("silk_cooldown")
    val VENOM_COOLDOWN = cooldown("venom_cooldown")
    val WATER_VULNERABILITY = cooldown("water_vulnerability")

    private fun effect(
        name: String,
        category: MobEffectCategory,
        color: Int
    ): DeferredHolder<MobEffect, MobEffect> =
        MOB_EFFECTS.register(name) { -> object : MobEffect(category, color) {} }

    private fun cooldown(name: String): DeferredHolder<MobEffect, MobEffect> =
        effect(name, MobEffectCategory.NEUTRAL, 0x000000)
}
