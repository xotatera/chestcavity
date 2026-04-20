package net.tigereye.chestcavity.registration

import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.food.FoodProperties

object CCFoodComponents {
    // Basic meats
    val ANIMAL_MUSCLE = food(1, 0.4f) { fast() }
    val SMALL_ANIMAL_MUSCLE = food(1, 0.2f) { fast() }
    val BURNT_MEAT_CHUNK = food(1, 0.8f) { fast() }
    val RAW_BUTCHERED_MEAT = food(2, 0.4f)
    val COOKED_BUTCHERED_MEAT = food(3, 0.8f)
    val RAW_ORGAN_MEAT = food(2, 0.6f) { fast() }
    val COOKED_ORGAN_MEAT = food(3, 1.2f) { fast() }
    val RAW_SAUSAGE = food(4, 0.4f)
    val COOKED_SAUSAGE = food(8, 0.8f)
    val RAW_RICH_SAUSAGE = food(4, 0.6f)
    val COOKED_RICH_SAUSAGE = food(8, 1.2f)
    val RAW_MINI_SAUSAGE = food(3, 0.4f)
    val COOKED_MINI_SAUSAGE = food(6, 0.8f)
    val RAW_RICH_MINI_SAUSAGE = food(3, 0.6f)
    val COOKED_RICH_MINI_SAUSAGE = food(6, 1.2f)

    // Rotten
    val ROTTEN_MUSCLE = food(1, 0.1f) { effect({ MobEffectInstance(MobEffects.HUNGER, 600, 0) }, 0.8f) }
    val ROTTEN_SAUSAGE = food(8, 0.1f) { effect({ MobEffectInstance(MobEffects.HUNGER, 600, 0) }, 0.8f) }

    // Toxic
    val INSECT_MUSCLE = food(1, 0.4f) { fast(); poisoned() }
    val RAW_TOXIC_MEAT = food(2, 0.4f) { poisoned() }
    val COOKED_TOXIC_MEAT = food(3, 0.8f) { nauseous() }
    val RAW_TOXIC_ORGAN_MEAT = food(2, 0.6f) { fast(); poisoned() }
    val COOKED_TOXIC_ORGAN_MEAT = food(3, 1.2f) { fast(); nauseous() }
    val RAW_TOXIC_SAUSAGE = food(4, 0.4f) { poisoned() }
    val COOKED_TOXIC_SAUSAGE = food(8, 0.8f) { nauseous() }
    val RAW_RICH_TOXIC_SAUSAGE = food(4, 0.6f) { poisoned() }
    val COOKED_RICH_TOXIC_SAUSAGE = food(8, 1.2f) { nauseous() }

    // Alien (ender)
    val ALIEN_MUSCLE = food(1, 0.4f) { fast(); effect({ MobEffectInstance(MobEffects.LEVITATION, 20) }, 1f) }
    val RAW_ALIEN_MEAT = food(2, 0.4f) { effect({ MobEffectInstance(MobEffects.LEVITATION, 80) }, 1f) }
    val COOKED_ALIEN_MEAT = food(3, 0.8f) { effect({ MobEffectInstance(MobEffects.SLOW_FALLING, 10, 1) }, 1f) }
    val RAW_ALIEN_ORGAN_MEAT = food(2, 0.6f) { fast(); effect({ MobEffectInstance(MobEffects.LEVITATION, 40) }, 1f) }
    val COOKED_ALIEN_ORGAN_MEAT = food(3, 1.2f) { fast(); effect({ MobEffectInstance(MobEffects.SLOW_FALLING, 15, 1) }, 1f) }
    val RAW_ALIEN_SAUSAGE = food(4, 0.4f) { effect({ MobEffectInstance(MobEffects.LEVITATION, 80) }, 1f) }
    val COOKED_ALIEN_SAUSAGE = food(8, 0.8f) { effect({ MobEffectInstance(MobEffects.SLOW_FALLING, 20, 1) }, 1f) }
    val RAW_RICH_ALIEN_SAUSAGE = food(4, 0.6f) { effect({ MobEffectInstance(MobEffects.LEVITATION, 320) }, 1f) }
    val COOKED_RICH_ALIEN_SAUSAGE = food(8, 1.2f) { effect({ MobEffectInstance(MobEffects.SLOW_FALLING, 40, 1) }, 1f) }

    // Dragon
    val DRAGON_MUSCLE = food(1, 0.4f) { fast(); alwaysEdible(); dragonBuff(300) }
    val RAW_DRAGON_MEAT = food(2, 0.4f) { alwaysEdible(); dragonBuff(900) }
    val COOKED_DRAGON_MEAT = food(3, 0.8f) { alwaysEdible(); dragonBuff(150, 1) }
    val RAW_DRAGON_ORGAN_MEAT = food(2, 0.6f) { fast(); alwaysEdible(); dragonBuff(1800) }
    val COOKED_DRAGON_ORGAN_MEAT = food(3, 1.2f) { fast(); alwaysEdible(); dragonBuff(300, 1) }
    val RAW_DRAGON_SAUSAGE = food(4, 0.4f) { alwaysEdible(); dragonBuff(9600) }
    val COOKED_DRAGON_SAUSAGE = food(8, 0.8f) { alwaysEdible(); dragonBuff(1800, 1) }
    val RAW_RICH_DRAGON_SAUSAGE = food(4, 0.6f) { alwaysEdible(); dragonBuff(19200) }
    val COOKED_RICH_DRAGON_SAUSAGE = food(8, 1.2f) { alwaysEdible(); dragonBuff(3600, 1) }
    val DRAGON_HEART = food(1, 0.4f) {
        fast(); alwaysEdible()
        effect({ MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 3) }, 1f)
        effect({ MobEffectInstance(MobEffects.DIG_SPEED, 600, 3) }, 1f)
        effect({ MobEffectInstance(MobEffects.POISON, 2, 3) }, 1f)
        effect({ MobEffectInstance(MobEffects.WITHER, 2, 3) }, 1f)
        effect({ MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 2, 3) }, 1f)
        effect({ MobEffectInstance(MobEffects.HUNGER, 2, 3) }, 1f)
        effect({ MobEffectInstance(MobEffects.CONFUSION, 2, 3) }, 1f)
        effect({ MobEffectInstance(MobEffects.BLINDNESS, 2, 3) }, 1f)
        effect({ MobEffectInstance(MobEffects.SLOW_FALLING, 2, 3) }, 1f)
        effect({ MobEffectInstance(MobEffects.LEVITATION, 2, 3) }, 1f)
    }

    // Human (prion risk) - note: actual risk comes from config, hardcoded 0.01f for now
    val HUMAN_MUSCLE = food(2, 0.4f) { fast(); prionRisk(0.01f) }
    val RAW_MAN_MEAT = food(3, 0.4f) { prionRisk(0.01f) }
    val COOKED_MAN_MEAT = food(4, 0.8f) { prionRisk(0.01f, amplifier = 0) }
    val RAW_HUMAN_ORGAN_MEAT = food(3, 0.6f) { fast(); prionRisk(0.01f) }
    val COOKED_HUMAN_ORGAN_MEAT = food(4, 1.2f) { fast(); prionRisk(0.01f, amplifier = 0) }
    val RAW_HUMAN_SAUSAGE = food(5, 0.4f) { prionRisk(0.01f) }
    val COOKED_HUMAN_SAUSAGE = food(9, 0.8f) { prionRisk(0.01f, amplifier = 0) }
    val RAW_RICH_HUMAN_SAUSAGE = food(5, 0.6f) { prionRisk(0.01f) }
    val COOKED_RICH_HUMAN_SAUSAGE = food(9, 1.2f) { prionRisk(0.01f, amplifier = 0) }

    // Utility foods
    val CUD = food(1, 0.1f)
    val FURNACE_POWER = food(1, 0.6f)

    // --- Builder DSL ---

    private fun food(
        nutrition: Int,
        saturation: Float,
        configure: FoodProperties.Builder.() -> Unit = {}
    ): FoodProperties =
        FoodProperties.Builder()
            .nutrition(nutrition)
            .saturationModifier(saturation)
            .apply(configure)
            .build()

    private fun FoodProperties.Builder.poisoned(): FoodProperties.Builder =
        effect({ MobEffectInstance(MobEffects.POISON, 80) }, 1f)

    private fun FoodProperties.Builder.nauseous(): FoodProperties.Builder =
        effect({ MobEffectInstance(MobEffects.CONFUSION, 160, 1) }, 1f)

    private fun FoodProperties.Builder.dragonBuff(duration: Int, amplifier: Int = 0): FoodProperties.Builder = this
        .effect({ MobEffectInstance(MobEffects.DAMAGE_BOOST, duration, amplifier) }, 1f)
        .effect({ MobEffectInstance(MobEffects.DIG_SPEED, duration, amplifier) }, 1f)

    private fun FoodProperties.Builder.prionRisk(chance: Float, amplifier: Int = 1): FoodProperties.Builder = this
        .effect({ MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 24000, amplifier) }, chance)
        .effect({ MobEffectInstance(MobEffects.DIG_SLOWDOWN, 24000, amplifier) }, chance)
        .effect({ MobEffectInstance(MobEffects.WEAKNESS, 24000, amplifier) }, chance)
}
