package com.onefizz.mixin;

import com.onefizz.OneFizzMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SimpleOption.class)
public class FullbrightMixin {

    @Unique private static SimpleOption<?> gammaOption = null;

    @Inject(method = "getValue", at = @At("RETURN"), cancellable = true)
    private void onGetValue(CallbackInfoReturnable<Object> cir) {
        if (OneFizzMod.modules == null) return;
        var fb = OneFizzMod.modules.getFullbright();
        if (!fb.isEnabled()) return;

        // Lazy-init + identity check (fast)
        if (gammaOption == null) {
            gammaOption = MinecraftClient.getInstance().options.getGamma();
        }
        SimpleOption<?> self = (SimpleOption<?>)(Object)this;
        if (self != gammaOption) return;

        cir.setReturnValue((double) fb.gamma);
    }
}
