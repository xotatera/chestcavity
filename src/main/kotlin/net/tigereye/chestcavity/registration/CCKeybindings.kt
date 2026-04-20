package net.tigereye.chestcavity.registration

import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.KeyMapping
import net.minecraft.resources.ResourceLocation
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent
import net.neoforged.neoforge.client.event.ClientTickEvent
import net.neoforged.neoforge.common.util.Lazy
import net.neoforged.neoforge.network.PacketDistributor
import net.tigereye.chestcavity.ChestCavity
import org.lwjgl.glfw.GLFW

@EventBusSubscriber(modid = ChestCavity.MODID, value = [Dist.CLIENT], bus = EventBusSubscriber.Bus.MOD)
object CCKeybindings {
    private const val CATEGORY = "category.${ChestCavity.MODID}.organ_abilities"

    val UTILITY_ABILITIES = binding("utility_abilities", GLFW.GLFW_KEY_V)
    val ATTACK_ABILITIES = binding("attack_abilities", GLFW.GLFW_KEY_R)

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

    private val ALL_BINDINGS = mutableListOf<KeyMapping>()

    private fun binding(name: String, key: Int): Lazy<KeyMapping> = Lazy.of {
        KeyMapping(
            "key.${ChestCavity.MODID}.$name",
            InputConstants.Type.KEYSYM,
            key,
            CATEGORY
        ).also { ALL_BINDINGS.add(it) }
    }

    @SubscribeEvent
    fun onRegisterKeys(event: RegisterKeyMappingsEvent) {
        ALL_BINDINGS.forEach { event.register(it) }
    }
}

@EventBusSubscriber(modid = ChestCavity.MODID, value = [Dist.CLIENT])
object CCKeybindingHandler {

    @SubscribeEvent
    fun onClientTick(event: ClientTickEvent.Post) {
        processKey(CCKeybindings.UTILITY_ABILITIES.get(), CCKeybindings.UTILITY_ABILITY_IDS)
        processKey(CCKeybindings.ATTACK_ABILITIES.get(), CCKeybindings.ATTACK_ABILITY_IDS)
    }

    private fun processKey(key: KeyMapping, abilities: List<ResourceLocation>) {
        while (key.consumeClick()) {
            abilities.forEach { id ->
                PacketDistributor.sendToServer(HotkeyPayload(id))
            }
        }
    }
}
