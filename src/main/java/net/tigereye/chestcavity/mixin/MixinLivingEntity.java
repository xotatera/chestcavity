package net.tigereye.chestcavity.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.tigereye.chestcavity.chestcavities.ChestCavityEntity;
import net.tigereye.chestcavity.chestcavities.instance.ChestCavityInstance;
import net.tigereye.chestcavity.chestcavities.instance.ChestCavityInstanceFactory;
import org.jetbrains.annotations.NotNull;
import net.tigereye.chestcavity.util.ChestCavityUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity implements ChestCavityEntity {

    @Unique
    private ChestCavityInstance chestcavity$instance;

    @Inject(at = @At("TAIL"), method = "<init>")
    private void chestcavity$init(EntityType<?> entityType, Level level, CallbackInfo ci) {
        chestcavity$instance = ChestCavityInstanceFactory.INSTANCE.create(
                entityType, (LivingEntity) (Object) this
        );
    }

    @NotNull
    @Override
    public ChestCavityInstance getChestCavityInstance() {
        return chestcavity$instance;
    }

    @Override
    public void setChestCavityInstance(@NotNull ChestCavityInstance instance) {
        chestcavity$instance = instance;
    }

    @ModifyVariable(
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;hasEffect(Lnet/minecraft/core/Holder;)Z"),
            method = "travel",
            ordinal = 1
    )
    private float chestcavity$modifySwimSpeed(float speed) {
        return speed * ChestCavityUtil.INSTANCE.applySwimSpeedInWater(chestcavity$instance);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void chestcavity$readData(CompoundTag tag, CallbackInfo ci) {
        chestcavity$instance.fromTag(tag, (LivingEntity) (Object) this);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void chestcavity$writeData(CompoundTag tag, CallbackInfo ci) {
        chestcavity$instance.toTag(tag);
    }
}
