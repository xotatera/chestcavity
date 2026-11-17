package net.tigereye.chestcavity.mixin;

import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.tigereye.chestcavity.util.OrganUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Cow.class)
public abstract class MixinCow {

    @Inject(method = "mobInteract(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",
            at = @At(value = "RETURN", ordinal = 0))
    private void chestcavity$milkSilk(Player player, InteractionHand hand,
                                      CallbackInfoReturnable<InteractionResult> cir) {
        OrganUtil.INSTANCE.milkSilk((Cow) (Object) this);
    }
}
