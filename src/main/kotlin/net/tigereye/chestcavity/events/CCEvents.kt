package net.tigereye.chestcavity.events

import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.RegisterCommandsEvent
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent
import net.neoforged.neoforge.event.tick.EntityTickEvent
import net.tigereye.chestcavity.ChestCavity
import net.tigereye.chestcavity.chestcavities.ChestCavityEntity
import net.tigereye.chestcavity.util.ChestCavityUtil

@EventBusSubscriber(modid = ChestCavity.MODID)
object CCEvents {

    @SubscribeEvent
    fun onEntityTick(event: EntityTickEvent.Post) {
        val entity = event.entity
        val cce = ChestCavityEntity.of(entity) ?: return
        ChestCavityUtil.onTick(cce.chestCavityInstance)
    }

    @SubscribeEvent
    fun onLivingDamage(event: LivingDamageEvent.Pre) {
        val target = event.entity
        val cce = ChestCavityEntity.of(target) ?: return
        val cc = cce.chestCavityInstance
        val newDamage = ChestCavityUtil.applyDefenses(cc, event.source, event.newDamage)
        event.newDamage = newDamage
    }

    @SubscribeEvent
    fun onLivingDeath(event: LivingDeathEvent) {
        val cce = ChestCavityEntity.of(event.entity) ?: return
        ChestCavityUtil.onDeath(cce)
    }

    @SubscribeEvent
    fun onPlayerClone(event: PlayerEvent.Clone) {
        val oldCce = ChestCavityEntity.of(event.original) ?: return
        val newCce = ChestCavityEntity.of(event.entity) ?: return
        newCce.chestCavityInstance.copyFrom(oldCce.chestCavityInstance)
    }

    @SubscribeEvent
    fun onPlayerBreakSpeed(event: PlayerEvent.BreakSpeed) {
        val cce = ChestCavityEntity.of(event.entity) ?: return
        event.newSpeed = ChestCavityUtil.applyNervesToMining(cce.chestCavityInstance, event.newSpeed)
    }

    @SubscribeEvent
    fun onEntityInteract(event: PlayerInteractEvent.EntityInteract) {
        val player = event.entity
        val stack = player.getItemInHand(event.hand)
        val opener = stack.item as? net.tigereye.chestcavity.items.ChestOpener ?: return
        val target = event.target as? net.minecraft.world.entity.LivingEntity ?: return

        opener.openChestCavity(player, target)
        event.isCanceled = true
    }

    @SubscribeEvent
    fun onRegisterCommands(event: RegisterCommandsEvent) {
        CCCommands.register(event.dispatcher)
    }
}
