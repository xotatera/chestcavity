package net.tigereye.chestcavity.listeners

import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.boss.wither.WitherBoss
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.GameRules
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent
import net.tigereye.chestcavity.CCConfig
import net.tigereye.chestcavity.ChestCavity
import net.tigereye.chestcavity.chestcavities.ChestCavityEntity
import net.tigereye.chestcavity.chestcavities.instance.ChestCavityInstance
import net.tigereye.chestcavity.chestcavities.organs.OrganManager
import net.tigereye.chestcavity.recipes.SalvageRecipe
import net.tigereye.chestcavity.registration.CCEnchantments
import net.tigereye.chestcavity.registration.CCTags
import kotlin.math.min

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

        // Take (and release) what the death transaction shed from the body during this drops phase.
        val bodyOrgans = cc.deathDrops
        cc.deathDrops = emptyList()

        val killer = event.source.entity as? LivingEntity
        val weapon = killer?.mainHandItem ?: ItemStack.EMPTY
        val bundle = rollOrganBundle(event, entity, cc, killer, weapon)

        // The original's second loot callback (LootRegister#ModifyLoot) only ever saw the mob's loot
        // and the bundle added before it. Player inventories never went through it, and neither did
        // organs that were transplanted into the body: those were shed by the death transaction and
        // are excluded here so a cleaver kill never butchers somebody's implant.
        if (entity is Player || killer == null) return
        val level = entity.level() as? ServerLevel ?: return
        val loot = event.drops.filter { drop -> bodyOrgans.none { it === drop } }

        if (weapon.`is`(CCTags.BUTCHERING_TOOL)) {
            val spared = sparedBundleOrgans(level, weapon, bundle)
            val salvageable = loot.filter { drop -> spared.none { it === drop } }
            salvage(level, entity, event.drops, salvageable)
        }

        // Curse of Malpractice on the killing weapon ruins every true organ that came out as loot.
        if (CCEnchantments.level(weapon, CCEnchantments.MALPRACTICE, level) > 0) {
            val curse = CCEnchantments.holder(level, CCEnchantments.MALPRACTICE) ?: return
            event.drops
                .filter { drop -> bodyOrgans.none { it === drop } }
                .map { it.item }
                .filter { isTrueOrgan(it) }
                .forEach { it.enchant(curse, 1) }
        }
    }

    /** Adds the random organ bundle to the drops and returns the item entities it added. */
    private fun rollOrganBundle(
        event: LivingDropsEvent,
        entity: LivingEntity,
        cc: ChestCavityInstance,
        killer: LivingEntity?,
        weapon: ItemStack,
    ): List<ItemEntity> {
        // Having opened the cavity already awarded the loot, so no random bundle on top of it.
        if (cc.opened) return emptyList()

        // L-06: the random organ bundle is extra mob loot, so it obeys the same gates as ordinary mob
        // loot: a player must have been credited for the kill recently (vanilla's
        // LootContextParams.LAST_DAMAGE_PLAYER condition), doMobLoot must be on and the entity must be
        // loot eligible. These gates wrap the random bundle only — organs actually transplanted into a
        // cavity are dropped by the cavity life cycle and must never be swallowed here.
        if (!event.isRecentlyHit) return emptyList()
        if (!entity.level().gameRules.getBoolean(GameRules.RULE_DOMOBLOOT)) return emptyList()
        if (!wouldDropLoot(entity)) return emptyList()

        // A-07: a missing living killer only means no looting/tool bonus, it is not a reason to skip
        // the bundle. This is how a player-credited environmental execution (fall, lava, explosion,
        // projectile) still rolls the bundle with looting 0.
        var lootingLevel = 0
        if (killer != null) {
            // Tomophobia: the killer is too squeamish to harvest anything.
            val world = entity.level()
            if (CCEnchantments.level(weapon, CCEnchantments.TOMOPHOBIA, world) > 0) return emptyList()
            lootingLevel = CCEnchantments.level(weapon, Enchantments.LOOTING, world) +
                2 * CCEnchantments.level(weapon, CCEnchantments.SURGICAL, world)
            // Butchering tool bonus
            if (weapon.`is`(CCTags.BUTCHERING_TOOL)) {
                lootingLevel = 10 + 10 * lootingLevel
            }
        }

        val added = cc.type.generateLootDrops(entity.random, lootingLevel).map { stack ->
            ItemEntity(entity.level(), entity.x, entity.y, entity.z, stack).also { it.setPickUpDelay(10) }
        }
        event.drops.addAll(added)
        return added
    }

    /**
     * The bundle organs a cleaver leaves whole. By default (the original behaviour) none: the bundle is
     * butchered like every other drop. With cleaver_salvages_organ_bundle off, all of them. With
     * surgical_spares_organs on, each bundle stack independently escapes with chance Surgical level / 3,
     * so level 3 and above keeps the whole bundle.
     */
    private fun sparedBundleOrgans(level: ServerLevel, weapon: ItemStack, bundle: List<ItemEntity>): List<ItemEntity> {
        if (bundle.isEmpty()) return emptyList()
        if (!CCConfig.CLEAVER_SALVAGES_ORGAN_BUNDLE.get()) return bundle
        if (!CCConfig.SURGICAL_SPARES_ORGANS.get()) return emptyList()
        val surgical = CCEnchantments.level(weapon, CCEnchantments.SURGICAL, level)
        if (surgical <= 0) return emptyList()
        return bundle.filter { level.random.nextInt(3) < surgical }
    }

    /**
     * Port of the original cleaver butchering (LootRegister#ModifyLoot): every salvageable drop is
     * pooled per salvage recipe and replaced by that recipe's output, `count / required` times.
     *
     * Two deliberate differences:
     * - The original threw away whatever did not fill a whole unit (a single muscle where four make one
     *   meat simply vanished). Here those leftovers stay as they were.
     * - The original took the first matching recipe in recipe-manager order, so an item matched by two
     *   recipes (human and insect intestines are both organ meat and sausage skin) came out as either,
     *   depending on load order. Here the most specific recipe wins: the one whose ingredient accepts
     *   the fewest items, then the lowest recipe id. Intestines therefore always become sausage skin,
     *   like every other intestine.
     */
    private fun salvage(
        level: ServerLevel,
        entity: LivingEntity,
        drops: MutableCollection<ItemEntity>,
        candidates: List<ItemEntity>,
    ) {
        val recipes = level.recipeManager.recipes
            .mapNotNull { holder -> (holder.value() as? SalvageRecipe)?.let { holder.id() to it } }
            .filter { (_, recipe) -> recipe.required > 0 }
            .sortedWith(compareBy({ (_, recipe) -> recipe.input.items.size }, { (id, _) -> id.toString() }))
            .map { it.second }
        if (recipes.isEmpty()) return

        val pools = LinkedHashMap<SalvageRecipe, MutableList<ItemEntity>>()
        for (drop in candidates) {
            val stack = drop.item
            if (stack.isEmpty || !stack.`is`(CCTags.SALVAGEABLE)) continue
            val recipe = recipes.firstOrNull { it.input.test(stack) } ?: continue
            pools.getOrPut(recipe) { mutableListOf() }.add(drop)
        }

        for ((recipe, pool) in pools) {
            val units = pool.sumOf { it.item.count } / recipe.required
            if (units <= 0) continue
            var toConsume = units * recipe.required
            for (drop in pool) {
                if (toConsume <= 0) break
                val count = drop.item.count
                val taken = min(count, toConsume)
                toConsume -= taken
                if (taken == count) drops.removeIf { it === drop }
                else drop.item = drop.item.copyWithCount(count - taken)
            }
            var remaining = units * recipe.outputStack.count
            val maxStack = recipe.outputStack.maxStackSize
            while (remaining > 0) {
                val n = min(remaining, maxStack)
                remaining -= n
                val out = ItemEntity(level, entity.x, entity.y, entity.z, recipe.outputStack.copyWithCount(n))
                out.setPickUpDelay(10)
                drops.add(out)
            }
        }
    }

    private fun isTrueOrgan(stack: ItemStack): Boolean {
        if (stack.isEmpty) return false
        val data = OrganManager.getEntry(stack) ?: return false
        return !data.pseudoOrgan
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
}
