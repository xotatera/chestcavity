package net.tigereye.chestcavity.recipes

import net.minecraft.core.HolderLookup
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.*
import net.minecraft.world.level.Level
import net.tigereye.chestcavity.registration.CCItems
import net.tigereye.chestcavity.registration.CCRecipes

class InfuseVenomGlandRecipe(category: CraftingBookCategory) : CustomRecipe(category) {

    override fun matches(container: CraftingInput, level: Level): Boolean {
        var foundGland = false
        var foundPotion = false
        for (i in 0 until container.size()) {
            val stack = container.getItem(i)
            if (stack.isEmpty) continue
            when (stack.item) {
                CCItems.VENOM_GLAND.get() -> {
                    if (foundGland) return false
                    foundGland = true
                }
                Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION -> {
                    if (foundPotion) return false
                    foundPotion = true
                }
                else -> return false
            }
        }
        return foundGland && foundPotion
    }

    override fun assemble(container: CraftingInput, registryAccess: HolderLookup.Provider): ItemStack {
        var gland: ItemStack? = null
        var potion: ItemStack? = null
        for (i in 0 until container.size()) {
            val stack = container.getItem(i)
            if (stack.isEmpty) continue
            when (stack.item) {
                CCItems.VENOM_GLAND.get() -> {
                    if (gland != null) return ItemStack.EMPTY
                    gland = stack
                }
                Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION -> {
                    if (potion != null) return ItemStack.EMPTY
                    potion = stack
                }
            }
        }
        if (gland == null || potion == null) return ItemStack.EMPTY
        val output = gland.copy()
        // TODO: transfer potion effects via DataComponent when organ data component is implemented
        return output
    }

    override fun canCraftInDimensions(width: Int, height: Int): Boolean = width * height >= 2

    override fun getSerializer(): RecipeSerializer<*> = CCRecipes.INFUSE_VENOM_GLAND.get()
}
