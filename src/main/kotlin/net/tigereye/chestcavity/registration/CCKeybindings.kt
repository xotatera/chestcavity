package net.tigereye.chestcavity.registration

import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent
import net.neoforged.neoforge.client.event.ClientTickEvent
import net.neoforged.neoforge.network.PacketDistributor
import net.tigereye.chestcavity.ChestCavity
import org.lwjgl.glfw.GLFW

@EventBusSubscriber(modid = ChestCavity.MODID, value = [Dist.CLIENT], bus = EventBusSubscriber.Bus.MOD)
object CCKeybindings {
    private const val CATEGORY = "category.${ChestCavity.MODID}.organ_abilities"

    val UTILITY_ABILITIES = KeyMapping(
        "key.${ChestCavity.MODID}.utility_abilities",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_V,
        CATEGORY
    )

    val ATTACK_ABILITIES = KeyMapping(
        "key.${ChestCavity.MODID}.attack_abilities",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_R,
        CATEGORY
    )

    // These individual bindings match the original mod's shared keypad-decimal default.
    val CREEPY = individualAbilityKey(CCOrganScores.CREEPY)
    val DRAGON_BREATH = individualAbilityKey(CCOrganScores.DRAGON_BREATH)
    val DRAGON_BOMBS = individualAbilityKey(CCOrganScores.DRAGON_BOMBS)
    val FORCEFUL_SPIT = individualAbilityKey(CCOrganScores.FORCEFUL_SPIT)
    val FURNACE_POWERED = individualAbilityKey(CCOrganScores.FURNACE_POWERED)
    val IRON_REPAIR = individualAbilityKey(CCOrganScores.IRON_REPAIR)
    val PYROMANCY = individualAbilityKey(CCOrganScores.PYROMANCY)
    val GHASTLY = individualAbilityKey(CCOrganScores.GHASTLY)
    val GRAZING = individualAbilityKey(CCOrganScores.GRAZING)
    val SHULKER_BULLETS = individualAbilityKey(CCOrganScores.SHULKER_BULLETS)
    val SILK = individualAbilityKey(CCOrganScores.SILK)

    private fun individualAbilityKey(organScore: ResourceLocation) = KeyMapping(
        "key.${ChestCavity.MODID}.${organScore.path}",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_KP_DECIMAL,
        CATEGORY
    )

    val INDIVIDUAL_ABILITY_KEYS = listOf(
        CREEPY to CCOrganScores.CREEPY,
        DRAGON_BREATH to CCOrganScores.DRAGON_BREATH,
        DRAGON_BOMBS to CCOrganScores.DRAGON_BOMBS,
        FORCEFUL_SPIT to CCOrganScores.FORCEFUL_SPIT,
        FURNACE_POWERED to CCOrganScores.FURNACE_POWERED,
        IRON_REPAIR to CCOrganScores.IRON_REPAIR,
        PYROMANCY to CCOrganScores.PYROMANCY,
        GHASTLY to CCOrganScores.GHASTLY,
        GRAZING to CCOrganScores.GRAZING,
        SHULKER_BULLETS to CCOrganScores.SHULKER_BULLETS,
        SILK to CCOrganScores.SILK,
    )

    val UTILITY_ABILITY_IDS = listOf(
        CCOrganScores.FURNACE_POWERED,
        CCOrganScores.IRON_REPAIR,
        CCOrganScores.GRAZING,
        CCOrganScores.SILK,
    )

    val ATTACK_ABILITY_IDS = listOf(
        CCOrganScores.CREEPY,
        CCOrganScores.DRAGON_BREATH,
        CCOrganScores.DRAGON_BOMBS,
        CCOrganScores.FORCEFUL_SPIT,
        CCOrganScores.PYROMANCY,
        CCOrganScores.GHASTLY,
        CCOrganScores.SHULKER_BULLETS,
    )

    @SubscribeEvent
    fun onRegisterKeys(event: RegisterKeyMappingsEvent) {
        event.register(UTILITY_ABILITIES)
        event.register(ATTACK_ABILITIES)
        INDIVIDUAL_ABILITY_KEYS.forEach { (key, _) -> event.register(key) }
    }
}

@EventBusSubscriber(modid = ChestCavity.MODID, value = [Dist.CLIENT])
object CCKeybindingHandler {

    @SubscribeEvent
    fun onClientTick(event: ClientTickEvent.Post) {
        if (Minecraft.getInstance().player == null) {
            discardClicks(CCKeybindings.UTILITY_ABILITIES)
            discardClicks(CCKeybindings.ATTACK_ABILITIES)
            CCKeybindings.INDIVIDUAL_ABILITY_KEYS.forEach { (key, _) -> discardClicks(key) }
            return
        }

        processKey(CCKeybindings.UTILITY_ABILITIES, CCKeybindings.UTILITY_ABILITY_IDS)
        processKey(CCKeybindings.ATTACK_ABILITIES, CCKeybindings.ATTACK_ABILITY_IDS)
        CCKeybindings.INDIVIDUAL_ABILITY_KEYS.forEach { (key, ability) ->
            processKey(key, listOf(ability))
        }
    }

    private fun discardClicks(key: KeyMapping) {
        while (key.consumeClick()) { }
    }

    private fun processKey(key: KeyMapping, abilities: List<ResourceLocation>) {
        while (key.consumeClick()) {
            abilities.forEach { id ->
                PacketDistributor.sendToServer(HotkeyPayload(id))
            }
        }
    }
}
