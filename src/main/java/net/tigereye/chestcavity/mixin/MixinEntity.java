package net.tigereye.chestcavity.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.tigereye.chestcavity.chestcavities.ChestCavityEntity;
import net.tigereye.chestcavity.registration.CCOrganScores;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Entity.class)
public abstract class MixinEntity {

    @ModifyVariable(
            method = "checkFallDamage(DZLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private double chestcavity$scaleFallDistance(double heightDifference) {
        if (heightDifference >= 0.0D) return heightDifference;

        ChestCavityEntity chestCavity = ChestCavityEntity.Companion.of((Entity) (Object) this);
        if (chestCavity == null) return heightDifference;

        float buoyant = chestCavity.getChestCavityInstance().organScore(CCOrganScores.INSTANCE.getBUOYANT());
        return heightDifference * (1.0D - buoyant / 3.0D);
    }
}
