package net.tigereye.chestcavity.listeners

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.LootTableLoadEvent
import net.tigereye.chestcavity.ChestCavity
import net.tigereye.chestcavity.registration.CCItems

/**
 * Port of the original LootRegister#register LootTableEvents.MODIFY hook: desert pyramid chests get
 * two extra pools of rotten ribs, rolled binomial(4, 0.25) and binomial(1, 0.3) times.
 */
@EventBusSubscriber(modid = ChestCavity.MODID)
object LootTableListeners {
    private val DESERT_PYRAMID = ResourceLocation.withDefaultNamespace("chests/desert_pyramid")

    @SubscribeEvent
    fun onLootTableLoad(event: LootTableLoadEvent) {
        if (event.name != DESERT_PYRAMID) return
        event.table.addPool(rottenRibPool("chestcavity:rotten_ribs", 4, 0.25f))
        event.table.addPool(rottenRibPool("chestcavity:rotten_rib_bonus", 1, 0.3f))
    }

    private fun rottenRibPool(name: String, trials: Int, chance: Float): LootPool =
        LootPool.lootPool()
            .name(name)
            .setRolls(BinomialDistributionGenerator.binomial(trials, chance))
            .add(LootItem.lootTableItem(CCItems.ROTTEN_RIB.get()))
            .build()
}
