package net.tigereye.chestcavity.items

import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.SimpleMenuProvider
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.tigereye.chestcavity.chestcavities.ChestCavityEntity
import net.tigereye.chestcavity.registration.CCOrganScores
import net.tigereye.chestcavity.ui.ChestCavityMenu
import net.tigereye.chestcavity.util.ChestCavityUtil

class ChestOpener(properties: Properties) : Item(properties) {

    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        val stack = player.getItemInHand(hand)
        // use() only fires for self-open (right-click air). Entity interact handles others.
        if (openChestCavity(player, player, shouldKnockback = false))
            return InteractionResultHolder.success(stack)
        return InteractionResultHolder.pass(stack)
    }

    fun openChestCavity(player: Player, target: LivingEntity, shouldKnockback: Boolean = true): Boolean {
        val cce = ChestCavityEntity.of(target) ?: return false
        val cc = cce.chestCavityInstance

        // Self always allowed; others must pass openability check
        if (target != player && !cc.type.isOpenable(cc)) {
            if (player.level().isClientSide) showFailureMessage(player, target)
            return false
        }

        // Ease of access: no damage, just sound
        if (cc.organScore(CCOrganScores.EASE_OF_ACCESS) > 0) {
            if (player.level().isClientSide) {
                player.playSound(SoundEvents.CHEST_OPEN, 0.75f, 1f)
            }
        } else {
            // Deal damage to open — generic for self (no knockback), player attack for others
            val damage = if (shouldKnockback)
                player.damageSources().playerAttack(player)
            else
                player.damageSources().generic()
            target.hurt(damage, 4f)
        }

        // If target died from the opening damage, don't show screen
        if (!target.isAlive) return true

        val inv = ChestCavityUtil.openChestCavity(cc)
        val playerCce = ChestCavityEntity.of(player)
        playerCce?.chestCavityInstance?.ccBeingOpened = cc

        val name = runCatching { "${target.displayName?.string}'s " }.getOrDefault("")
        player.openMenu(SimpleMenuProvider(
            { id, playerInv, _ -> ChestCavityMenu(id, playerInv, inv) },
            Component.literal("${name}Chest Cavity")
        ))
        return true
    }

    private fun showFailureMessage(player: Player, target: LivingEntity) {
        if (!target.getItemBySlot(EquipmentSlot.CHEST).isEmpty) {
            player.displayClientMessage(Component.literal("Target's chest is obstructed"), true)
            player.playSound(SoundEvents.CHAIN_HIT, 0.75f, 1f)
        } else {
            player.displayClientMessage(Component.literal("Target is too healthy to open"), true)
            player.playSound(SoundEvents.ARMOR_EQUIP_TURTLE.value(), 0.75f, 1f)
        }
    }
}
