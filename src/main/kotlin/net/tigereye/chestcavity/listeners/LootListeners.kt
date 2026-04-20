package net.tigereye.chestcavity.listeners

import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent
import net.tigereye.chestcavity.ChestCavity
import net.tigereye.chestcavity.chestcavities.ChestCavityEntity

@EventBusSubscriber(modid = ChestCavity.MODID)
object LootListeners {

    @SubscribeEvent
    fun onLivingDrops(event: LivingDropsEvent) {
        val source = event.source
        val killer = source.entity as? Player ?: return
        val entity = event.entity
        val cce = ChestCavityEntity.of(entity) ?: return
        val cc = cce.chestCavityInstance

        if (cc.opened) return

        val looting = 0 // TODO: get looting enchantment level from killer's weapon
        val drops = cc.type.generateLootDrops(entity.random, looting)

        drops.forEach { stack ->
            val itemEntity = ItemEntity(entity.level(), entity.x, entity.y, entity.z, stack)
            itemEntity.setPickUpDelay(10)
            event.drops.add(itemEntity)
        }
    }
}
