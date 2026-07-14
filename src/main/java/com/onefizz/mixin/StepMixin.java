package com.onefizz.mixin;

import com.onefizz.OneFizzMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class StepMixin {

    @Inject(method = "getStepHeight", at = @At("RETURN"), cancellable = true)
    private void onGetStepHeight(CallbackInfoReturnable<Float> cir) {
        try {
            if (OneFizzMod.modules == null) return;
            var step = OneFizzMod.modules.getStep();
            if (!step.isEnabled()) return;

            LivingEntity self = (LivingEntity)(Object)this;
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc == null || mc.player == null || self != mc.player) return;

            float h = Math.min(step.height, 2.5f);
            cir.setReturnValue(h);
        } catch (Throwable ignored) {}
    }
}
