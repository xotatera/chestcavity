package net.tigereye.chestcavity.events

import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent
import net.tigereye.chestcavity.ChestCavity
import net.tigereye.chestcavity.registration.CCMenus
import net.tigereye.chestcavity.ui.ChestCavityScreen

@EventBusSubscriber(modid = ChestCavity.MODID, value = [Dist.CLIENT], bus = EventBusSubscriber.Bus.MOD)
object CCClientEvents {

    @SubscribeEvent
    fun onRegisterScreens(event: RegisterMenuScreensEvent) {
        event.register(CCMenus.CHEST_CAVITY.get(), ::ChestCavityScreen)
    }
}
