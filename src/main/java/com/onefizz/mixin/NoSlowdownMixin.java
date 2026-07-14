package com.onefizz.mixin;

import com.onefizz.OneFizzMod;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * NoSlowdown — возвращаем false из isUsingItem() чтобы убрать slowdown при движении.
 * Это НЕ влияет на soul sand / honey (те используют другой механизм).
 * Только отменяет замедление от еды/щита/лука.
 */
@Mixin(ClientPlayerEntity.class)
public class NoSlowdownMixin {

    @Inject(method = "isUsingItem", at = @At("HEAD"), cancellable = true)
    private void onIsUsingItem(CallbackInfoReturnable<Boolean> cir) {
        if (OneFizzMod.modules == null) return;
        var ns = OneFizzMod.modules.getNoSlowdown();
        if (!ns.isEnabled() || !ns.items) return;
        // Возвращаем false — движок не применит slowdown от использования предмета
        cir.setReturnValue(false);
    }
}
