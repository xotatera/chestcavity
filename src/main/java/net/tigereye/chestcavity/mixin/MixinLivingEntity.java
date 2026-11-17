package net.tigereye.chestcavity.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.effect.MobEffectInstance;
import net.tigereye.chestcavity.events.CCEvents;
import net.tigereye.chestcavity.chestcavities.ChestCavityEntity;
import net.tigereye.chestcavity.chestcavities.instance.ChestCavityInstance;
import net.tigereye.chestcavity.chestcavities.instance.ChestCavityInstanceFactory;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

    /**
     * LivingEntity.eat(Level, ItemStack, FoodProperties) is shared by ordinary finish-use eating
     * and direct consumers such as Fox.aiStep -> ItemStack.finishUsingItem. Player.eat invokes
     * super.eat exactly once, so HEAD applies prion risk once before the food stack is consumed.
     */
    @Inject(method = "eat(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/food/FoodProperties;)Lnet/minecraft/world/item/ItemStack;", at = @At("HEAD"))
    private void chestcavity$applyHumanFoodPrionRisk(Level level, ItemStack food, FoodProperties properties, CallbackInfoReturnable<ItemStack> cir) {
        CCEvents.INSTANCE.applyHumanFoodPrionRisk((LivingEntity) (Object) this, food);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void chestcavity$readData(CompoundTag tag, CallbackInfo ci) {
        chestcavity$instance.fromTag(tag, (LivingEntity) (Object) this);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void chestcavity$writeData(CompoundTag tag, CallbackInfo ci) {
        chestcavity$instance.toTag(tag);
    }

    /**
     * Swaps the addEffect parameter before anything reads it (L-10), mirroring
     * the original mod's ModifyVariable on addStatusEffect. Only the
     * (MobEffectInstance, Entity) overload is targeted: the single-argument
     * overload delegates into it, so a plain-name match would shorten twice.
     * Shortening here keeps addEffect's return value true (R2), preserves the
     * source entity argument, and leaves vanilla's own merge path untouched.
     */
    @ModifyVariable(at = @At("HEAD"), ordinal = 0,
            method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z")
    private MobEffectInstance chestcavity$modifyEffectDuration(MobEffectInstance effect) {
        MobEffectInstance shortened = CCEvents.INSTANCE.shortenAppliedEffect(
                (LivingEntity) (Object) this, effect);
        return shortened == null ? effect : shortened;
    }
}
