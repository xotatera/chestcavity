package net.tigereye.chestcavity.registration

import net.minecraft.core.registries.Registries
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import net.tigereye.chestcavity.ChestCavity
import net.tigereye.chestcavity.recipes.InfuseVenomGlandRecipe
import net.tigereye.chestcavity.recipes.SalvageRecipe

object CCRecipes {
    val RECIPE_SERIALIZERS: DeferredRegister<RecipeSerializer<*>> =
        DeferredRegister.create(Registries.RECIPE_SERIALIZER, ChestCavity.MODID)

    val RECIPE_TYPES: DeferredRegister<RecipeType<*>> =
        DeferredRegister.create(Registries.RECIPE_TYPE, ChestCavity.MODID)

    val SALVAGE_RECIPE_TYPE: DeferredHolder<RecipeType<*>, RecipeType<SalvageRecipe>> =
        RECIPE_TYPES.register("crafting_salvage") { -> object : RecipeType<SalvageRecipe> {} }

    val SALVAGE_RECIPE_SERIALIZER: DeferredHolder<RecipeSerializer<*>, RecipeSerializer<SalvageRecipe>> =
        RECIPE_SERIALIZERS.register("crafting_salvage") { ->
            object : RecipeSerializer<SalvageRecipe> {
                override fun codec() = SalvageRecipe.CODEC
                override fun streamCodec() = SalvageRecipe.STREAM_CODEC
            }
        }

    val INFUSE_VENOM_GLAND: DeferredHolder<RecipeSerializer<*>, RecipeSerializer<InfuseVenomGlandRecipe>> =
        RECIPE_SERIALIZERS.register("crafting_special_infuse_venom_gland") { ->
            SimpleCraftingRecipeSerializer(::InfuseVenomGlandRecipe)
        }
}
