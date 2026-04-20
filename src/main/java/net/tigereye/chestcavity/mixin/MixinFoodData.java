package net.tigereye.chestcavity.mixin;

import kotlin.Pair;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.tigereye.chestcavity.chestcavities.ChestCavityEntity;
import net.tigereye.chestcavity.chestcavities.instance.ChestCavityInstance;
import net.tigereye.chestcavity.listeners.OrganFoodListeners;
import net.tigereye.chestcavity.registration.CCOrganScores;
import net.tigereye.chestcavity.util.ChestCavityUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodData.class)
public class MixinFoodData {

    @Shadow
    private int tickTimer;

    @Shadow
    private float exhaustionLevel;

    private Player chestcavity$player;

    @Inject(at = @At("HEAD"), method = "tick")
    private void chestcavity$tickMetabolism(Player player, CallbackInfo ci) {
        chestcavity$player = player;
        ChestCavityEntity cce = ChestCavityEntity.Companion.of(player);
        if (cce == null) return;
        ChestCavityInstance cc = cce.getChestCavityInstance();
        tickTimer = ChestCavityUtil.INSTANCE.applySpleenMetabolism(cc, tickTimer);
    }

    @ModifyVariable(at = @At("HEAD"), method = "addExhaustion", ordinal = 0, argsOnly = true)
    private float chestcavity$modifyExhaustion(float exhaustion) {
        if (chestcavity$player == null) return exhaustion;
        ChestCavityEntity cce = ChestCavityEntity.Companion.of(chestcavity$player);
        if (cce == null) return exhaustion;
        ChestCavityInstance cc = cce.getChestCavityInstance();

        float enduranceDiff = cc.organScore(CCOrganScores.INSTANCE.getENDURANCE())
                - cc.getType().getDefaultOrganScore(CCOrganScores.INSTANCE.getENDURANCE());
        if (enduranceDiff > 0) {
            return exhaustion / (1f + enduranceDiff / 2f);
        } else if (enduranceDiff < 0) {
            return exhaustion * (1f - enduranceDiff / 2f);
        }
        return exhaustion;
    }
}
