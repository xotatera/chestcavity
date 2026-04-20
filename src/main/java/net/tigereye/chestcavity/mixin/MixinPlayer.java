package net.tigereye.chestcavity.mixin;

import kotlin.Pair;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.tigereye.chestcavity.listeners.OrganFoodListeners;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
public abstract class MixinPlayer {

    @Redirect(method = "eat", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/food/FoodData;eat(Lnet/minecraft/world/food/FoodProperties;)V"))
    private void chestcavity$modifyEat(net.minecraft.world.food.FoodData foodData, FoodProperties food,
                                        net.minecraft.world.level.Level level, ItemStack stack, FoodProperties foodArg) {
        Player player = (Player) (Object) this;
        Pair<Integer, Float> modified = OrganFoodListeners.INSTANCE.modifyFoodValues(stack, food, player);
        if (modified != null) {
            FoodProperties modifiedFood = new FoodProperties.Builder()
                    .nutrition(modified.getFirst())
                    .saturationModifier(modified.getSecond())
                    .build();
            foodData.eat(modifiedFood);
        } else {
            foodData.eat(food);
        }
    }
}
