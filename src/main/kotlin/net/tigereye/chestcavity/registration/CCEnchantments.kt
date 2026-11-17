package net.tigereye.chestcavity.registration

import net.minecraft.core.Holder
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.level.Level
import net.tigereye.chestcavity.ChestCavity

/**
 * The four Chest Cavity enchantments. In 1.21 enchantments are data driven: their definitions live in
 * `data/chestcavity/enchantment/` and their behaviour is implemented where the original read them
 * (loot, compatibility, death, tooltip), so this object only names them and reads their levels.
 *
 * Levels are read straight from the stack's enchantment component by key, which needs no registry
 * access and therefore works the same on the client (tooltips) and on the server.
 */
object CCEnchantments {
    /** I: organ is never rejected. II: also stays in the body when a player dies. */
    val O_NEGATIVE: ResourceKey<Enchantment> = key("o_negative")
    /** +2 looting per level for the organ bundle roll. */
    val SURGICAL: ResourceKey<Enchantment> = key("surgical")
    /** Curse: organs dropped by kills with this weapon become unsafe (always rejected). */
    val MALPRACTICE: ResourceKey<Enchantment> = key("malpractice")
    /** The killer gets no organ bundle at all. */
    val TOMOPHOBIA: ResourceKey<Enchantment> = key("tomophobia")

    private fun key(path: String): ResourceKey<Enchantment> =
        ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(ChestCavity.MODID, path))

    /** Level stored on the stack. Used for organs (compatibility, death, tooltip) on either side. */
    fun level(stack: ItemStack, key: ResourceKey<Enchantment>): Int {
        if (stack.isEmpty) return 0
        for (entry in stack.tagEnchantments.entrySet()) {
            if (entry.key.`is`(key)) return entry.intValue
        }
        return 0
    }

    /**
     * Gameplay level of a weapon enchantment on the server. Goes through NeoForge's
     * IItemStackExtension#getEnchantmentLevel so items that grant enchantments by other means count too.
     */
    fun level(stack: ItemStack, key: ResourceKey<Enchantment>, world: Level): Int {
        if (stack.isEmpty) return 0
        val holder = holder(world, key) ?: return 0
        return stack.getEnchantmentLevel(holder)
    }

    fun holder(level: Level, key: ResourceKey<Enchantment>): Holder<Enchantment>? =
        level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(key).orElse(null)
}
