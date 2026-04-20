package net.tigereye.chestcavity.registration

import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.KeyMapping
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
    }
}

@EventBusSubscriber(modid = ChestCavity.MODID, value = [Dist.CLIENT])
object CCKeybindingHandler {

    @SubscribeEvent
    fun onClientTick(event: ClientTickEvent.Post) {
        processKey(CCKeybindings.UTILITY_ABILITIES, CCKeybindings.UTILITY_ABILITY_IDS)
        processKey(CCKeybindings.ATTACK_ABILITIES, CCKeybindings.ATTACK_ABILITY_IDS)
    }

    private fun processKey(key: KeyMapping, abilities: List<ResourceLocation>) {
        while (key.consumeClick()) {
            abilities.forEach { id ->
                PacketDistributor.sendToServer(HotkeyPayload(id))
            }
        }
    }
}
