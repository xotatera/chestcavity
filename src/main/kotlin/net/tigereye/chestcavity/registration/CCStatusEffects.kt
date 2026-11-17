package net.tigereye.chestcavity.registration

import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.damagesource.DamageType
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectCategory
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.food.FoodProperties
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import net.tigereye.chestcavity.CCConfig
import net.tigereye.chestcavity.ChestCavity
import net.tigereye.chestcavity.chestcavities.ChestCavityEntity
import net.tigereye.chestcavity.listeners.OrganFoodListeners

object CCStatusEffects {
    val MOB_EFFECTS: DeferredRegister<MobEffect> =
        DeferredRegister.create(Registries.MOB_EFFECT, ChestCavity.MODID)

    val ORGAN_REJECTION = MOB_EFFECTS.register("organ_rejection") { -> OrganRejectionEffect() }
    val ARROW_DODGE_COOLDOWN = cooldown("arrow_dodge_cooldown")
    val DRAGON_BOMB_COOLDOWN = cooldown("dragon_bomb_cooldown")
    val DRAGON_BREATH_COOLDOWN = cooldown("dragon_breath_cooldown")
    val EXPLOSION_COOLDOWN = cooldown("explosion_cooldown")
    val FORCEFUL_SPIT_COOLDOWN = cooldown("forceful_spit_cooldown")
    val FURNACE_POWER = MOB_EFFECTS.register("furnace_power") { -> FurnacePowerEffect() }
    val GHASTLY_COOLDOWN = cooldown("ghastly_cooldown")
    val IRON_REPAIR_COOLDOWN = cooldown("iron_repair_cooldown")
    val PYROMANCY_COOLDOWN = cooldown("pyromancy_cooldown")
    val RUMINATING = MOB_EFFECTS.register("ruminating") { -> RuminatingEffect() }
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

    /**
     * Feed a food item through the digestion/nutrition organ pipeline, mirroring
     * MixinPlayer's redirect of Player.eat so periodic feeding (furnace power,
     * rumination) honours organ scores instead of using raw food values (A-05).
     */
    fun eatThroughOrganPipeline(player: Player, stack: ItemStack) {
        val food = stack.get(DataComponents.FOOD) ?: return
        val modified = OrganFoodListeners.modifyFoodValues(stack, food, player)
        if (modified != null) {
            player.foodData.eat(
                FoodProperties.Builder()
                    .nutrition(modified.first)
                    .saturationModifier(modified.second)
                    .build()
            )
        } else {
            player.foodData.eat(food)
        }
    }
}

/** Organ rejection bites with a bypasses-armor damage type once per cycle (A-05). */
class OrganRejectionEffect : MobEffect(MobEffectCategory.NEUTRAL, 0xC8FF00) {

    override fun shouldApplyEffectTickThisTick(duration: Int, amplifier: Int): Boolean = duration <= 1

    override fun applyEffectTick(entity: LivingEntity, amplifier: Int): Boolean {
        if (!entity.level().isClientSide) {
            entity.hurt(
                entity.damageSources().source(CCDamageTypes.ORGAN_REJECTION),
                CCConfig.ORGAN_REJECTION_DAMAGE.get().toFloat()
            )
        }
        return true
    }
}

/** Chews the cud at a fixed interval while the effect lasts (A-05). */
class RuminatingEffect : MobEffect(MobEffectCategory.BENEFICIAL, 0xC8FF00) {

    override fun shouldApplyEffectTickThisTick(duration: Int, amplifier: Int): Boolean =
        duration % CCConfig.RUMINATION_TIME.get() == 1

    override fun applyEffectTick(entity: LivingEntity, amplifier: Int): Boolean {
        if (entity is Player && !entity.level().isClientSide) {
            CCStatusEffects.eatThroughOrganPipeline(entity, ItemStack(CCItems.CUD.get()))
        }
        return true
    }
}

/** Advances furnace progress every tick and feeds the player every 200 ticks per amplifier level (A-05). */
class FurnacePowerEffect : MobEffect(MobEffectCategory.BENEFICIAL, 0xC8FF00) {

    override fun shouldApplyEffectTickThisTick(duration: Int, amplifier: Int): Boolean = true

    override fun applyEffectTick(entity: LivingEntity, amplifier: Int): Boolean {
        if (entity !is Player || entity.level().isClientSide) return true
        val cc = ChestCavityEntity.of(entity)?.chestCavityInstance ?: return true
        cc.furnaceProgress++
        if (cc.furnaceProgress >= 200) {
            cc.furnaceProgress = 0
            repeat(amplifier + 1) {
                CCStatusEffects.eatThroughOrganPipeline(entity, ItemStack(CCItems.FURNACE_POWER.get()))
            }
        }
        return true
    }
}

object CCDamageTypes {
    val HEARTBLEED: ResourceKey<DamageType> = ResourceKey.create(
        Registries.DAMAGE_TYPE,
        ResourceLocation.fromNamespaceAndPath(ChestCavity.MODID, "heartbleed")
    )
    val ORGAN_REJECTION: ResourceKey<DamageType> = ResourceKey.create(
        Registries.DAMAGE_TYPE,
        ResourceLocation.fromNamespaceAndPath(ChestCavity.MODID, "organ_rejection")
    )
}
