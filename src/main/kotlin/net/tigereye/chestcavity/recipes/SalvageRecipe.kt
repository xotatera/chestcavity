package net.tigereye.chestcavity.recipes

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.*
import net.minecraft.world.level.Level
import net.tigereye.chestcavity.registration.CCRecipes

class SalvageRecipe(
    val input: Ingredient,
    val required: Int,
    val outputStack: ItemStack,
) : CraftingRecipe {

    override fun matches(container: CraftingInput, level: Level): Boolean {
        var count = 0
        for (i in 0 until container.size()) {
            val stack = container.getItem(i)
            if (stack.isEmpty) continue
            if (!input.test(stack)) return false
            count++
        }
        return count > 0 && count % required == 0
    }

    override fun assemble(container: CraftingInput, registryAccess: HolderLookup.Provider): ItemStack {
        var count = 0
        for (i in 0 until container.size()) {
            val stack = container.getItem(i)
            if (stack.isEmpty) continue
            if (!input.test(stack)) return ItemStack.EMPTY
            count++
        }
        if (count == 0 || count % required != 0) return ItemStack.EMPTY
        val resultCount = (count / required) * outputStack.count
        if (resultCount > outputStack.maxStackSize) return ItemStack.EMPTY
        return outputStack.copyWithCount(resultCount)
    }

    override fun canCraftInDimensions(width: Int, height: Int): Boolean = true

    override fun getResultItem(registryAccess: HolderLookup.Provider): ItemStack = outputStack.copy()

    override fun getSerializer(): RecipeSerializer<*> = CCRecipes.SALVAGE_RECIPE_SERIALIZER.get()

    override fun category(): CraftingBookCategory = CraftingBookCategory.MISC

    companion object {
        val CODEC: MapCodec<SalvageRecipe> = RecordCodecBuilder.mapCodec { builder ->
            builder.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter { it.input },
                Codec.INT.optionalFieldOf("required", 1).forGetter { it.required },
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter { it.outputStack },
            ).apply(builder, ::SalvageRecipe)
        }

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, SalvageRecipe> =
            StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, { it.input },
                ByteBufCodecs.INT, { it.required },
                ItemStack.STREAM_CODEC, { it.outputStack },
                ::SalvageRecipe
            )
    }
}
