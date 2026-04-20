package net.tigereye.chestcavity.registration

import net.minecraft.resources.ResourceLocation
import net.tigereye.chestcavity.ChestCavity

object CCOrganScores {
    private fun id(path: String) = ResourceLocation.fromNamespaceAndPath(ChestCavity.MODID, path)

    // Basic organ abilities
    val LUCK = id("luck")
    val HEALTH = id("health")
    val NUTRITION = id("nutrition")
    val FILTRATION = id("filtration")
    val DETOXIFICATION = id("detoxification")
    val BREATH_RECOVERY = id("breath_recovery")
    val BREATH_CAPACITY = id("breath_capacity")
    val ENDURANCE = id("endurance")
    val STRENGTH = id("strength")
    val SPEED = id("speed")
    val DEFENSE = id("defense")
    val NERVES = id("nerves")
    val METABOLISM = id("metabolism")
    val DIGESTION = id("digestion")
    val INCOMPATIBILITY = id("incompatibility")

    // Passive abilities
    val ARROW_DODGING = id("arrow_dodging")
    val BUFF_PURGING = id("buff_purging")
    val BUOYANT = id("buoyant")
    val DESTRUCTIVE_COLLISIONS = id("destructive_collisions")
    val EASE_OF_ACCESS = id("ease_of_access")
    val FIRE_RESISTANT = id("fire_resistant")
    val GLOWING = id("glowing")
    val HYDROALLERGENIC = id("hydroallergenic")
    val HYDROPHOBIA = id("hydrophobia")
    val IMPACT_RESISTANT = id("impact_resistant")
    val KNOCKBACK_RESISTANT = id("knockback_resistant")
    val LAUNCHING = id("launching")
    val LEAPING = id("leaping")
    val SWIM_SPEED = id("swim_speed")
    val WATERBREATH = id("water_breath")
    val WITHERED = id("withered")

    // Activated abilities
    val CREEPY = id("creepy")
    val DRAGON_BOMBS = id("dragon_bombs")
    val DRAGON_BREATH = id("dragon_breath")
    val EXPLOSIVE = id("explosive")
    val FORCEFUL_SPIT = id("forceful_spit")
    val GHASTLY = id("ghastly")
    val GRAZING = id("grazing")
    val PYROMANCY = id("pyromancy")
    val SHULKER_BULLETS = id("shulker_bullets")
    val SILK = id("silk")

    // Food abilities
    val CRYSTALSYNTHESIS = id("crystalsynthesis")
    val PHOTOSYNTHESIS = id("photosynthesis")
    val CARNIVOROUS_DIGESTION = id("carnivorous_digestion")
    val CARNIVOROUS_NUTRITION = id("carnivorous_nutrition")
    val FURNACE_POWERED = id("furnace_powered")
    val HERBIVOROUS_DIGESTION = id("herbivorous_digestion")
    val HERBIVOROUS_NUTRITION = id("herbivorous_nutrition")
    val IRON_REPAIR = id("iron_repair")
    val ROT_DIGESTION = id("rot_digestion")
    val ROTGUT = id("rotgut")

    // On-hit abilities
    val VENOMOUS = id("venomous")
}
