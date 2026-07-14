package com.onefizz.mixin;

import com.onefizz.OneFizzMod;
import net.minecraft.client.render.BackgroundRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BackgroundRenderer.class)
public class NoRenderFogMixin {

    @Inject(method = "applyFog", at = @At("HEAD"), cancellable = true)
    private static void cancelFog(CallbackInfo ci) {
        if (OneFizzMod.modules.getNoRender().isEnabled()
            && OneFizzMod.modules.getNoRender().noFog) {
            ci.cancel();
        }
    }
}
