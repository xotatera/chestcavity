package net.tigereye.chestcavity

import com.mojang.logging.LogUtils
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.fml.config.ModConfig
import net.neoforged.neoforge.common.NeoForge
import net.tigereye.chestcavity.registration.CCAttachments
import net.tigereye.chestcavity.registration.CCCreativeTabs
import net.tigereye.chestcavity.registration.CCDataComponents
import net.tigereye.chestcavity.registration.CCItems
import net.tigereye.chestcavity.registration.CCMenus
import net.tigereye.chestcavity.registration.CCRecipes
import net.tigereye.chestcavity.registration.CCStatusEffects

@Mod(ChestCavity.MODID)
class ChestCavity(modEventBus: IEventBus, modContainer: ModContainer) {
    companion object {
        const val MODID = "chestcavity"
        const val DEBUG_MODE = false
        val LOGGER = LogUtils.getLogger()!!
    }

    init {
        CCItems.ITEMS.register(modEventBus)
        CCStatusEffects.MOB_EFFECTS.register(modEventBus)
        CCCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus)
        CCMenus.MENUS.register(modEventBus)
        CCAttachments.ATTACHMENT_TYPES.register(modEventBus)
        CCRecipes.RECIPE_SERIALIZERS.register(modEventBus)
        CCRecipes.RECIPE_TYPES.register(modEventBus)
        CCDataComponents.DATA_COMPONENTS.register(modEventBus)

        modContainer.registerConfig(ModConfig.Type.COMMON, CCConfig.SPEC)

        NeoForge.EVENT_BUS.register(this)
        LOGGER.info("Chest Cavity initializing")
    }
}
