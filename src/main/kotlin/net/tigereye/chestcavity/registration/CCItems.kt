package net.tigereye.chestcavity.registration

import net.minecraft.world.food.FoodProperties
import net.minecraft.world.item.Item
import net.minecraft.world.item.SwordItem
import net.minecraft.world.item.Tiers
import net.neoforged.neoforge.registries.DeferredItem
import net.neoforged.neoforge.registries.DeferredRegister
import net.tigereye.chestcavity.ChestCavity
import net.tigereye.chestcavity.items.ChestOpener
import net.tigereye.chestcavity.items.CreeperAppendix
import net.tigereye.chestcavity.items.VenomGland

object CCItems {
    val ITEMS: DeferredRegister.Items = DeferredRegister.createItems(ChestCavity.MODID)

    // --- Tools ---
    val CHEST_OPENER: DeferredItem<Item> = ITEMS.register("chest_opener") { ->
        ChestOpener(Item.Properties().stacksTo(1))
    }
    val WOODEN_CLEAVER = cleaver("wooden_cleaver", Tiers.WOOD)
    val STONE_CLEAVER = cleaver("stone_cleaver", Tiers.STONE)
    val GOLD_CLEAVER = cleaver("gold_cleaver", Tiers.GOLD)
    val IRON_CLEAVER = cleaver("iron_cleaver", Tiers.IRON)
    val DIAMOND_CLEAVER = cleaver("diamond_cleaver", Tiers.DIAMOND)
    val NETHERITE_CLEAVER = cleaver("netherite_cleaver", Tiers.NETHERITE) { fireResistant() }

    // --- Human organs ---
    val HUMAN_APPENDIX = organ("appendix", CCFoodComponents.RAW_HUMAN_ORGAN_MEAT)
    val HUMAN_HEART = organ("heart", CCFoodComponents.RAW_HUMAN_ORGAN_MEAT)
    val HUMAN_INTESTINE = organ("intestine", CCFoodComponents.RAW_HUMAN_ORGAN_MEAT)
    val HUMAN_KIDNEY = organ("kidney", CCFoodComponents.RAW_HUMAN_ORGAN_MEAT)
    val HUMAN_LIVER = organ("liver", CCFoodComponents.RAW_HUMAN_ORGAN_MEAT)
    val HUMAN_LUNG = organ("lung", CCFoodComponents.RAW_HUMAN_ORGAN_MEAT)
    val HUMAN_MUSCLE = item("muscle") { stacksTo(16).food(CCFoodComponents.HUMAN_MUSCLE) }
    val HUMAN_RIB = item("rib") { stacksTo(4) }
    val HUMAN_SPINE = item("spine") { stacksTo(1) }
    val HUMAN_SPLEEN = organ("spleen", CCFoodComponents.RAW_HUMAN_ORGAN_MEAT)
    val HUMAN_STOMACH = organ("stomach", CCFoodComponents.RAW_HUMAN_ORGAN_MEAT)

    // --- Rotten organs ---
    val ROTTEN_APPENDIX = organ("rotten_appendix", CCFoodComponents.ROTTEN_MUSCLE)
    val ROTTEN_HEART = organ("rotten_heart", CCFoodComponents.ROTTEN_MUSCLE)
    val ROTTEN_INTESTINE = organ("rotten_intestine", CCFoodComponents.ROTTEN_MUSCLE)
    val ROTTEN_KIDNEY = organ("rotten_kidney", CCFoodComponents.ROTTEN_MUSCLE)
    val ROTTEN_LIVER = organ("rotten_liver", CCFoodComponents.ROTTEN_MUSCLE)
    val ROTTEN_LUNG = organ("rotten_lung", CCFoodComponents.ROTTEN_MUSCLE)
    val ROTTEN_MUSCLE = item("rotten_muscle") { stacksTo(16).food(CCFoodComponents.ROTTEN_MUSCLE) }
    val ROTTEN_RIB = item("rotten_rib") { stacksTo(4) }
    val ROTTEN_SPINE = item("rotten_spine") { stacksTo(1) }
    val ROTTEN_SPLEEN = organ("rotten_spleen", CCFoodComponents.ROTTEN_MUSCLE)
    val ROTTEN_STOMACH = organ("rotten_stomach", CCFoodComponents.ROTTEN_MUSCLE)
    val WITHERED_RIB = item("withered_rib") { stacksTo(4) }
    val WITHERED_SPINE = item("withered_spine") { stacksTo(1) }
    val WRITHING_SOULSAND = item("writhing_soulsand") { stacksTo(16) }

    // --- Animal organs ---
    val ANIMAL_APPENDIX = organ("animal_appendix", CCFoodComponents.RAW_ORGAN_MEAT)
    val ANIMAL_HEART = organ("animal_heart", CCFoodComponents.RAW_ORGAN_MEAT)
    val ANIMAL_INTESTINE = organ("animal_intestine", CCFoodComponents.RAW_ORGAN_MEAT)
    val ANIMAL_KIDNEY = organ("animal_kidney", CCFoodComponents.RAW_ORGAN_MEAT)
    val ANIMAL_LIVER = organ("animal_liver", CCFoodComponents.RAW_ORGAN_MEAT)
    val ANIMAL_LUNG = organ("animal_lung", CCFoodComponents.RAW_ORGAN_MEAT)
    val ANIMAL_MUSCLE = item("animal_muscle") { stacksTo(16).food(CCFoodComponents.ANIMAL_MUSCLE) }
    val ANIMAL_RIB = item("animal_rib") { stacksTo(4) }
    val ANIMAL_SPINE = item("animal_spine") { stacksTo(1) }
    val ANIMAL_SPLEEN = organ("animal_spleen", CCFoodComponents.RAW_ORGAN_MEAT)
    val ANIMAL_STOMACH = organ("animal_stomach", CCFoodComponents.RAW_ORGAN_MEAT)
    val AQUATIC_MUSCLE = item("aquatic_muscle") { stacksTo(16).food(CCFoodComponents.ANIMAL_MUSCLE) }
    val FISH_MUSCLE = item("fish_muscle") { stacksTo(16).food(CCFoodComponents.ANIMAL_MUSCLE) }
    val GILLS = organ("gills", CCFoodComponents.RAW_ORGAN_MEAT)
    val LLAMA_LUNG = organ("llama_lung", CCFoodComponents.RAW_ORGAN_MEAT)
    val CARNIVORE_STOMACH = organ("carnivore_stomach", CCFoodComponents.RAW_ORGAN_MEAT)
    val CARNIVORE_INTESTINE = organ("carnivore_intestine", CCFoodComponents.RAW_ORGAN_MEAT)
    val HERBIVORE_RUMEN = organ("herbivore_rumen", CCFoodComponents.RAW_ORGAN_MEAT)
    val HERBIVORE_STOMACH = organ("herbivore_stomach", CCFoodComponents.RAW_ORGAN_MEAT)
    val HERBIVORE_INTESTINE = organ("herbivore_intestine", CCFoodComponents.RAW_ORGAN_MEAT)
    val BRUTISH_MUSCLE = item("brutish_muscle") { stacksTo(16).food(CCFoodComponents.ANIMAL_MUSCLE) }
    val SWIFT_MUSCLE = item("swift_muscle") { stacksTo(16).food(CCFoodComponents.ANIMAL_MUSCLE) }
    val SPRINGY_MUSCLE = item("springy_muscle") { stacksTo(16).food(CCFoodComponents.ANIMAL_MUSCLE) }

    // --- Fireproof organs ---
    val FIREPROOF_APPENDIX = organ("fireproof_appendix", CCFoodComponents.RAW_ORGAN_MEAT)
    val FIREPROOF_HEART = organ("fireproof_heart", CCFoodComponents.RAW_ORGAN_MEAT)
    val FIREPROOF_INTESTINE = organ("fireproof_intestine", CCFoodComponents.RAW_ORGAN_MEAT)
    val FIREPROOF_KIDNEY = organ("fireproof_kidney", CCFoodComponents.RAW_ORGAN_MEAT)
    val FIREPROOF_LIVER = organ("fireproof_liver", CCFoodComponents.RAW_ORGAN_MEAT)
    val FIREPROOF_LUNG = organ("fireproof_lung", CCFoodComponents.RAW_ORGAN_MEAT)
    val FIREPROOF_MUSCLE = item("fireproof_muscle") { stacksTo(16).food(CCFoodComponents.ANIMAL_MUSCLE) }
    val FIREPROOF_RIB = item("fireproof_rib") { stacksTo(4) }
    val FIREPROOF_SPINE = item("fireproof_spine") { stacksTo(1) }
    val FIREPROOF_SPLEEN = organ("fireproof_spleen", CCFoodComponents.RAW_ORGAN_MEAT)
    val FIREPROOF_STOMACH = organ("fireproof_stomach", CCFoodComponents.RAW_ORGAN_MEAT)

    // --- Small animal organs ---
    val SMALL_ANIMAL_APPENDIX = organ("small_animal_appendix", CCFoodComponents.SMALL_ANIMAL_MUSCLE)
    val SMALL_ANIMAL_HEART = organ("small_animal_heart", CCFoodComponents.SMALL_ANIMAL_MUSCLE)
    val SMALL_ANIMAL_INTESTINE = organ("small_animal_intestine", CCFoodComponents.SMALL_ANIMAL_MUSCLE)
    val SMALL_ANIMAL_KIDNEY = organ("small_animal_kidney", CCFoodComponents.SMALL_ANIMAL_MUSCLE)
    val SMALL_ANIMAL_LIVER = organ("small_animal_liver", CCFoodComponents.SMALL_ANIMAL_MUSCLE)
    val SMALL_ANIMAL_LUNG = organ("small_animal_lung", CCFoodComponents.SMALL_ANIMAL_MUSCLE)
    val SMALL_ANIMAL_MUSCLE = item("small_animal_muscle") { stacksTo(16).food(CCFoodComponents.SMALL_ANIMAL_MUSCLE) }
    val SMALL_ANIMAL_RIB = item("small_animal_rib") { stacksTo(4) }
    val SMALL_ANIMAL_SPINE = item("small_animal_spine") { stacksTo(1) }
    val SMALL_ANIMAL_SPLEEN = organ("small_animal_spleen", CCFoodComponents.SMALL_ANIMAL_MUSCLE)
    val SMALL_ANIMAL_STOMACH = organ("small_animal_stomach", CCFoodComponents.SMALL_ANIMAL_MUSCLE)
    val RABBIT_HEART = organ("rabbit_heart", CCFoodComponents.SMALL_ANIMAL_MUSCLE)
    val SMALL_AQUATIC_MUSCLE = item("small_aquatic_muscle") { stacksTo(16).food(CCFoodComponents.SMALL_ANIMAL_MUSCLE) }
    val SMALL_FISH_MUSCLE = item("small_fish_muscle") { stacksTo(16).food(CCFoodComponents.SMALL_ANIMAL_MUSCLE) }
    val SMALL_SPRINGY_MUSCLE = item("small_springy_muscle") { stacksTo(16).food(CCFoodComponents.SMALL_ANIMAL_MUSCLE) }
    val SMALL_GILLS = organ("small_gills", CCFoodComponents.SMALL_ANIMAL_MUSCLE)
    val SMALL_CARNIVORE_STOMACH = organ("small_carnivore_stomach", CCFoodComponents.SMALL_ANIMAL_MUSCLE)
    val SMALL_CARNIVORE_INTESTINE = organ("small_carnivore_intestine", CCFoodComponents.SMALL_ANIMAL_MUSCLE)
    val SMALL_HERBIVORE_STOMACH = organ("small_herbivore_stomach", CCFoodComponents.SMALL_ANIMAL_MUSCLE)
    val SMALL_HERBIVORE_INTESTINE = organ("small_herbivore_intestine", CCFoodComponents.SMALL_ANIMAL_MUSCLE)

    // --- Insect organs ---
    val INSECT_HEART = organ("insect_heart", CCFoodComponents.RAW_TOXIC_ORGAN_MEAT)
    val INSECT_INTESTINE = organ("insect_intestine", CCFoodComponents.RAW_TOXIC_ORGAN_MEAT)
    val INSECT_LUNG = organ("insect_lung", CCFoodComponents.RAW_TOXIC_ORGAN_MEAT)
    val INSECT_MUSCLE = item("insect_muscle") { stacksTo(16).food(CCFoodComponents.INSECT_MUSCLE) }
    val INSECT_STOMACH = organ("insect_stomach", CCFoodComponents.RAW_TOXIC_ORGAN_MEAT)
    val INSECT_CAECA = organ("insect_caeca", CCFoodComponents.RAW_TOXIC_ORGAN_MEAT)
    val SILK_GLAND = organ("silk_gland", CCFoodComponents.RAW_TOXIC_ORGAN_MEAT)
    val VENOM_GLAND: DeferredItem<Item> = ITEMS.register("venom_gland") { ->
        VenomGland(Item.Properties().stacksTo(1).food(CCFoodComponents.RAW_TOXIC_ORGAN_MEAT))
    }

    // --- Ender organs ---
    val ENDER_APPENDIX = organ("ender_appendix", CCFoodComponents.RAW_ALIEN_ORGAN_MEAT)
    val ENDER_HEART = organ("ender_heart", CCFoodComponents.RAW_ALIEN_ORGAN_MEAT)
    val ENDER_INTESTINE = organ("ender_intestine", CCFoodComponents.RAW_ALIEN_ORGAN_MEAT)
    val ENDER_KIDNEY = organ("ender_kidney", CCFoodComponents.RAW_ALIEN_ORGAN_MEAT)
    val ENDER_LIVER = organ("ender_liver", CCFoodComponents.RAW_ALIEN_ORGAN_MEAT)
    val ENDER_LUNG = organ("ender_lung", CCFoodComponents.RAW_ALIEN_ORGAN_MEAT)
    val ENDER_MUSCLE = item("ender_muscle") { stacksTo(16).food(CCFoodComponents.ALIEN_MUSCLE) }
    val ENDER_RIB = item("ender_rib") { stacksTo(4) }
    val ENDER_SPINE = item("ender_spine") { stacksTo(1) }
    val ENDER_SPLEEN = organ("ender_spleen", CCFoodComponents.RAW_ALIEN_ORGAN_MEAT)
    val ENDER_STOMACH = organ("ender_stomach", CCFoodComponents.RAW_ALIEN_ORGAN_MEAT)

    // --- Dragon organs ---
    val DRAGON_APPENDIX = organ("dragon_appendix", CCFoodComponents.RAW_DRAGON_ORGAN_MEAT)
    val DRAGON_HEART = organ("dragon_heart", CCFoodComponents.DRAGON_HEART)
    val DRAGON_KIDNEY = organ("dragon_kidney", CCFoodComponents.RAW_DRAGON_ORGAN_MEAT)
    val DRAGON_LIVER = organ("dragon_liver", CCFoodComponents.RAW_DRAGON_ORGAN_MEAT)
    val DRAGON_LUNG = organ("dragon_lung", CCFoodComponents.RAW_DRAGON_ORGAN_MEAT)
    val DRAGON_MUSCLE = item("dragon_muscle") { stacksTo(16).food(CCFoodComponents.DRAGON_MUSCLE) }
    val DRAGON_RIB = item("dragon_rib") { stacksTo(4) }
    val DRAGON_SPINE = item("dragon_spine") { stacksTo(1) }
    val DRAGON_SPLEEN = organ("dragon_spleen", CCFoodComponents.RAW_DRAGON_ORGAN_MEAT)
    val MANA_REACTOR = organ("mana_reactor", CCFoodComponents.RAW_DRAGON_ORGAN_MEAT)

    // --- Blaze ---
    val ACTIVE_BLAZE_ROD = item("active_blaze_rod") { stacksTo(3) }
    val BLAZE_SHELL = item("blaze_shell") { stacksTo(4) }
    val BLAZE_CORE = item("blaze_core") { stacksTo(1) }

    // --- Ghast / Creeper / misc ---
    val GAS_BLADDER = item("gas_bladder") { stacksTo(1) }
    val VOLATILE_STOMACH = item("volatile_stomach") { stacksTo(1) }

    // --- Golem ---
    val GOLEM_CABLE = item("golem_cable") { stacksTo(1) }
    val GOLEM_PLATING = item("golem_plating") { stacksTo(4) }
    val GOLEM_CORE = item("golem_core") { stacksTo(1) }
    val INNER_FURNACE = item("inner_furnace") { stacksTo(1) }
    val PISTON_MUSCLE = item("piston_muscle") { stacksTo(16) }
    val IRON_SCRAP = item("iron_scrap") { stacksTo(64) }

    // --- Saltwater / special ---
    val SALTWATER_HEART = item("saltwater_heart") { stacksTo(1) }
    val SALTWATER_LUNG = item("saltwater_lung") { stacksTo(1) }
    val SALTWATER_MUSCLE = item("saltwater_muscle") { stacksTo(16) }
    val CREEPER_APPENDIX: DeferredItem<Item> = ITEMS.register("creeper_appendix") { ->
        CreeperAppendix(Item.Properties().stacksTo(1))
    }
    val SHIFTING_LEAVES = item("shifting_leaves") { stacksTo(16) }
    val SHULKER_SPLEEN = item("shulker_spleen") { stacksTo(1) }

    // --- Crafting materials ---
    val SAUSAGE_SKIN = item("sausage_skin") { stacksTo(64) }
    val MINI_SAUSAGE_SKIN = item("mini_sausage_skin") { stacksTo(64) }

    // --- Cooked food products ---
    val BURNT_MEAT_CHUNK = food("burnt_meat_chunk", CCFoodComponents.BURNT_MEAT_CHUNK)
    val RAW_ORGAN_MEAT = food("raw_organ_meat", CCFoodComponents.RAW_ORGAN_MEAT)
    val COOKED_ORGAN_MEAT = food("cooked_organ_meat", CCFoodComponents.COOKED_ORGAN_MEAT)
    val RAW_BUTCHERED_MEAT = food("raw_butchered_meat", CCFoodComponents.RAW_BUTCHERED_MEAT)
    val COOKED_BUTCHERED_MEAT = food("cooked_butchered_meat", CCFoodComponents.COOKED_BUTCHERED_MEAT)
    val RAW_SAUSAGE = food("raw_sausage", CCFoodComponents.RAW_SAUSAGE)
    val COOKED_SAUSAGE = food("sausage", CCFoodComponents.COOKED_SAUSAGE)
    val RAW_RICH_SAUSAGE = food("raw_rich_sausage", CCFoodComponents.RAW_RICH_SAUSAGE)
    val COOKED_RICH_SAUSAGE = food("rich_sausage", CCFoodComponents.COOKED_RICH_SAUSAGE)
    val RAW_MINI_SAUSAGE = food("raw_mini_sausage", CCFoodComponents.RAW_MINI_SAUSAGE)
    val COOKED_MINI_SAUSAGE = food("mini_sausage", CCFoodComponents.COOKED_MINI_SAUSAGE)
    val RAW_RICH_MINI_SAUSAGE = food("raw_rich_mini_sausage", CCFoodComponents.RAW_RICH_MINI_SAUSAGE)
    val COOKED_RICH_MINI_SAUSAGE = food("rich_mini_sausage", CCFoodComponents.COOKED_RICH_MINI_SAUSAGE)
    val ROTTEN_SAUSAGE_ITEM = food("rotten_sausage", CCFoodComponents.ROTTEN_SAUSAGE)

    val RAW_TOXIC_ORGAN_MEAT = food("raw_toxic_organ_meat", CCFoodComponents.RAW_TOXIC_ORGAN_MEAT)
    val COOKED_TOXIC_ORGAN_MEAT = food("cooked_toxic_organ_meat", CCFoodComponents.COOKED_TOXIC_ORGAN_MEAT)
    val RAW_TOXIC_MEAT = food("raw_toxic_meat", CCFoodComponents.RAW_TOXIC_MEAT)
    val COOKED_TOXIC_MEAT = food("cooked_toxic_meat", CCFoodComponents.COOKED_TOXIC_MEAT)
    val RAW_TOXIC_SAUSAGE = food("raw_toxic_sausage", CCFoodComponents.RAW_TOXIC_SAUSAGE)
    val COOKED_TOXIC_SAUSAGE = food("toxic_sausage", CCFoodComponents.COOKED_TOXIC_SAUSAGE)
    val RAW_RICH_TOXIC_SAUSAGE = food("raw_rich_toxic_sausage", CCFoodComponents.RAW_RICH_TOXIC_SAUSAGE)
    val COOKED_RICH_TOXIC_SAUSAGE = food("rich_toxic_sausage", CCFoodComponents.COOKED_RICH_TOXIC_SAUSAGE)

    val RAW_HUMAN_ORGAN_MEAT = food("raw_human_organ_meat", CCFoodComponents.RAW_HUMAN_ORGAN_MEAT)
    val COOKED_HUMAN_ORGAN_MEAT = food("cooked_human_organ_meat", CCFoodComponents.COOKED_HUMAN_ORGAN_MEAT)
    val RAW_MAN_MEAT = food("raw_man_meat", CCFoodComponents.RAW_MAN_MEAT)
    val COOKED_MAN_MEAT = food("cooked_man_meat", CCFoodComponents.COOKED_MAN_MEAT)
    val RAW_HUMAN_SAUSAGE = food("raw_human_sausage", CCFoodComponents.RAW_HUMAN_SAUSAGE)
    val COOKED_HUMAN_SAUSAGE = food("human_sausage", CCFoodComponents.COOKED_HUMAN_SAUSAGE)
    val RAW_RICH_HUMAN_SAUSAGE = food("raw_rich_human_sausage", CCFoodComponents.RAW_RICH_HUMAN_SAUSAGE)
    val COOKED_RICH_HUMAN_SAUSAGE = food("rich_human_sausage", CCFoodComponents.COOKED_RICH_HUMAN_SAUSAGE)

    val RAW_ALIEN_ORGAN_MEAT = food("raw_alien_organ_meat", CCFoodComponents.RAW_ALIEN_ORGAN_MEAT)
    val COOKED_ALIEN_ORGAN_MEAT = food("cooked_alien_organ_meat", CCFoodComponents.COOKED_ALIEN_ORGAN_MEAT)
    val RAW_ALIEN_MEAT = food("raw_alien_meat", CCFoodComponents.RAW_ALIEN_MEAT)
    val COOKED_ALIEN_MEAT = food("cooked_alien_meat", CCFoodComponents.COOKED_ALIEN_MEAT)
    val RAW_ALIEN_SAUSAGE = food("raw_alien_sausage", CCFoodComponents.RAW_ALIEN_SAUSAGE)
    val COOKED_ALIEN_SAUSAGE = food("alien_sausage", CCFoodComponents.COOKED_ALIEN_SAUSAGE)
    val RAW_RICH_ALIEN_SAUSAGE = food("raw_rich_alien_sausage", CCFoodComponents.RAW_RICH_ALIEN_SAUSAGE)
    val COOKED_RICH_ALIEN_SAUSAGE = food("rich_alien_sausage", CCFoodComponents.COOKED_RICH_ALIEN_SAUSAGE)

    val RAW_DRAGON_ORGAN_MEAT = food("raw_dragon_organ_meat", CCFoodComponents.RAW_DRAGON_ORGAN_MEAT)
    val COOKED_DRAGON_ORGAN_MEAT = food("cooked_dragon_organ_meat", CCFoodComponents.COOKED_DRAGON_ORGAN_MEAT)
    val RAW_DRAGON_MEAT = food("raw_dragon_meat", CCFoodComponents.RAW_DRAGON_MEAT)
    val COOKED_DRAGON_MEAT = food("cooked_dragon_meat", CCFoodComponents.COOKED_DRAGON_MEAT)
    val RAW_DRAGON_SAUSAGE = food("raw_dragon_sausage", CCFoodComponents.RAW_DRAGON_SAUSAGE)
    val COOKED_DRAGON_SAUSAGE = food("dragon_sausage", CCFoodComponents.COOKED_DRAGON_SAUSAGE)
    val RAW_RICH_DRAGON_SAUSAGE = food("raw_rich_dragon_sausage", CCFoodComponents.RAW_RICH_DRAGON_SAUSAGE)
    val COOKED_RICH_DRAGON_SAUSAGE = food("rich_dragon_sausage", CCFoodComponents.COOKED_RICH_DRAGON_SAUSAGE)

    val CUD = food("cud", CCFoodComponents.CUD)
    val FURNACE_POWER = food("furnace_power", CCFoodComponents.FURNACE_POWER)

    // --- Factory helpers ---

    private fun item(name: String, props: Item.Properties.() -> Item.Properties = { this }): DeferredItem<Item> =
        ITEMS.register(name) { -> Item(Item.Properties().props()) }

    private fun organ(name: String, food: FoodProperties): DeferredItem<Item> =
        ITEMS.register(name) { -> Item(Item.Properties().stacksTo(1).food(food)) }

    private fun food(name: String, food: FoodProperties): DeferredItem<Item> =
        ITEMS.register(name) { -> Item(Item.Properties().stacksTo(64).food(food)) }

    private fun cleaver(
        name: String,
        tier: Tiers,
        extraProps: Item.Properties.() -> Item.Properties = { this }
    ): DeferredItem<SwordItem> =
        ITEMS.register(name) { ->
            SwordItem(
                tier,
                Item.Properties()
                    .attributes(SwordItem.createAttributes(tier, 3, -2.4f))
                    .extraProps()
            )
        }
}
