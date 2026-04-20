package net.tigereye.chestcavity.events

import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.AddReloadListenerEvent
import net.tigereye.chestcavity.ChestCavity
import net.tigereye.chestcavity.chestcavities.organs.OrganManager
import net.tigereye.chestcavity.chestcavities.types.ChestCavityTypeManager

@EventBusSubscriber(modid = ChestCavity.MODID)
object CCServerEvents {

    @SubscribeEvent
    fun onAddReloadListeners(event: AddReloadListenerEvent) {
        event.addListener(OrganManager)
        event.addListener(ChestCavityTypeManager)
    }
}
