package net.tigereye.chestcavity.mixin;

import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.sounds.SoundSource;
import net.tigereye.chestcavity.util.OrganUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Sheep.class)
public abstract class MixinSheep {

    @Inject(method = "shear(Lnet/minecraft/sounds/SoundSource;)V", at = @At("HEAD"))
    private void chestcavity$shearSilk(SoundSource soundSource, CallbackInfo ci) {
        OrganUtil.INSTANCE.shearSilk((Sheep) (Object) this);
    }
}
