package net.tigereye.chestcavity.listeners

import net.minecraft.core.registries.Registries
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.boss.wither.WitherBoss
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.level.GameRules
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent
import net.tigereye.chestcavity.ChestCavity
import net.tigereye.chestcavity.chestcavities.ChestCavityEntity
import net.tigereye.chestcavity.registration.CCTags

@EventBusSubscriber(modid = ChestCavity.MODID)
object LootListeners {

    @SubscribeEvent
    fun onLivingDrops(event: LivingDropsEvent) {
        val entity = event.entity
        val cce = ChestCavityEntity.of(entity) ?: return
        val cc = cce.chestCavityInstance

        // A-07: the nether star correction must not depend on the killer being a living entity.
        // Vanilla WitherBoss#dropCustomDeathLoot already spawned exactly one star before this event
        // fired, so at most that one star is removed when the cavity was opened and the star is gone
        // from it. The decision is taken on the pre-event inventory, before any bundle organ is added
        // below, and never touches organs that were transplanted into the cavity.
        if (entity is WitherBoss && cc.opened) {
            val hasStarInCavity = (0 until cc.inventory.containerSize).any {
                cc.inventory.getItem(it).`is`(Items.NETHER_STAR)
            }
            if (!hasStarInCavity) {
                removeOneNetherStar(event.drops)
            }
        }

        // Having opened the cavity already awarded the loot, so no random bundle on top of it.
        if (cc.opened) return

        // L-06: the random organ bundle is extra mob loot, so it obeys the same gates as ordinary mob
        // loot: a player must have been credited for the kill recently (vanilla's
        // LootContextParams.LAST_DAMAGE_PLAYER condition), doMobLoot must be on and the entity must be
        // loot eligible. These gates wrap the random bundle only — organs actually transplanted into a
        // cavity are dropped by the cavity life cycle and must never be swallowed here.
        if (!event.isRecentlyHit) return
        if (!entity.level().gameRules.getBoolean(GameRules.RULE_DOMOBLOOT)) return
        if (!wouldDropLoot(entity)) return

        // A-07: a missing living killer only means no looting/tool bonus, it is not a reason to skip
        // the bundle. This is how a player-credited environmental execution (fall, lava, explosion,
        // projectile) still rolls the bundle with looting 0.
        val killer = event.source.entity as? LivingEntity
        var lootingLevel = if (killer == null) 0 else getLootingLevel(killer.mainHandItem, entity)
        // Butchering tool bonus
        if (killer != null && killer.mainHandItem.`is`(CCTags.BUTCHERING_TOOL)) {
            lootingLevel = 10 + 10 * lootingLevel
        }

        val drops = cc.type.generateLootDrops(entity.random, lootingLevel)
        drops.forEach { stack ->
            val itemEntity = ItemEntity(entity.level(), entity.x, entity.y, entity.z, stack)
            itemEntity.setPickUpDelay(10)
            event.drops.add(itemEntity)
        }
    }

    /**
     * Removes at most one nether star drop — the one vanilla spawned in dropCustomDeathLoot — instead
     * of deleting every star in the collection. Removing all of them would also delete stars added by
     * other sources and would hide a real duplication bug rather than fix it.
     */
    private fun removeOneNetherStar(drops: MutableCollection<ItemEntity>) {
        val star = drops.firstOrNull { it.item.`is`(Items.NETHER_STAR) } ?: return
        drops.removeIf { it === star }
    }

    /**
     * Semantic stand-in for the protected LivingEntity#shouldDropLoot(). In 1.21.1 it has exactly two
     * implementations: LivingEntity returns !isBaby() and Monster returns true unconditionally
     * (verified against the NeoForge sources of this version). A custom mob that overrides it is not
     * visible from here without an access transformer, which this branch deliberately does not add.
     */
    private fun wouldDropLoot(entity: LivingEntity): Boolean =
        entity is Monster || !entity.isBaby

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
