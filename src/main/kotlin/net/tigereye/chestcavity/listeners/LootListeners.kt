package net.tigereye.chestcavity.listeners

import net.minecraft.core.registries.Registries
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent
import net.tigereye.chestcavity.ChestCavity
import net.tigereye.chestcavity.chestcavities.ChestCavityEntity

@EventBusSubscriber(modid = ChestCavity.MODID)
object LootListeners {

    @SubscribeEvent
    fun onLivingDrops(event: LivingDropsEvent) {
        val killer = event.source.entity as? LivingEntity ?: return
        val entity = event.entity
        val cce = ChestCavityEntity.of(entity) ?: return
        val cc = cce.chestCavityInstance

        if (cc.opened) return

        val lootingLevel = getLootingLevel(killer.mainHandItem, entity)
        val drops = cc.type.generateLootDrops(entity.random, lootingLevel)

        drops.forEach { stack ->
            val itemEntity = ItemEntity(entity.level(), entity.x, entity.y, entity.z, stack)
            itemEntity.setPickUpDelay(10)
            event.drops.add(itemEntity)
        }
    }

    private fun getLootingLevel(weapon: ItemStack, target: LivingEntity): Int {
        val registry = target.level().registryAccess()
            .lookupOrThrow(Registries.ENCHANTMENT)
        val looting = registry.listElements()
            .filter { it.key().location().path == "looting" }
            .findFirst()
            .orElse(null) ?: return 0
        return EnchantmentHelper.getItemEnchantmentLevel(looting, weapon)
    }
}
