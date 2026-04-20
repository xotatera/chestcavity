package net.tigereye.chestcavity.events

import net.minecraft.network.chat.Component
import net.minecraft.world.SimpleMenuProvider
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
import net.tigereye.chestcavity.registration.CCItems
import net.tigereye.chestcavity.registration.CCOrganScores
import net.tigereye.chestcavity.ui.ChestCavityMenu
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
        if (stack.item != CCItems.CHEST_OPENER.get()) return

        val target = event.target as? net.minecraft.world.entity.LivingEntity ?: return
        val cce = ChestCavityEntity.of(target) ?: return
        val cc = cce.chestCavityInstance

        if (!cc.type.isOpenable(cc)) return
        if (cc.organScore(CCOrganScores.EASE_OF_ACCESS) <= 0) {
            target.hurt(player.damageSources().playerAttack(player), 4f)
        }
        if (!target.isAlive) return

        val inv = ChestCavityUtil.openChestCavity(cc)
        val name = runCatching { target.displayName?.string ?: "" }.getOrDefault("")
        player.openMenu(SimpleMenuProvider(
            { id, playerInv, _ -> ChestCavityMenu(id, playerInv, inv) },
            Component.literal("${name}'s Chest Cavity")
        ))
        event.isCanceled = true
    }

    @SubscribeEvent
    fun onRegisterCommands(event: RegisterCommandsEvent) {
        CCCommands.register(event.dispatcher)
    }
}
