package net.tigereye.chestcavity.events

import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.AddReloadListenerEvent
import net.neoforged.neoforge.event.OnDatapackSyncEvent
import net.tigereye.chestcavity.ChestCavity
import net.tigereye.chestcavity.chestcavities.ChestCavityEntity
import net.tigereye.chestcavity.chestcavities.instance.ChestCavityInstanceFactory
import net.tigereye.chestcavity.chestcavities.organs.OrganManager
import net.tigereye.chestcavity.chestcavities.types.ChestCavityTypeManager
import net.tigereye.chestcavity.util.ChestCavityUtil

@EventBusSubscriber(modid = ChestCavity.MODID)
object CCServerEvents {

    @SubscribeEvent
    fun onAddReloadListeners(event: AddReloadListenerEvent) {
        event.addListener(OrganManager)
        event.addListener(ChestCavityTypeManager)
    }

    @SubscribeEvent
    fun onDatapackSync(event: OnDatapackSyncEvent) {
        val players = event.player?.let { listOf(it) }
            ?: event.playerList?.players
            ?: return

        players.forEach { player ->
            val cce = ChestCavityEntity.of(player) ?: return@forEach
            val cc = cce.chestCavityInstance
            cc.type = ChestCavityInstanceFactory.resolveType(player.type)
            ChestCavityUtil.evaluateChestCavity(cc)
        }
    }
}
