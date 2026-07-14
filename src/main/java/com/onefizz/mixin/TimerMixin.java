package com.onefizz.mixin;

import com.onefizz.OneFizzMod;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * TimerMixin — перехватывает beginRenderTick у RenderTickCounter.Dynamic.
 * Возвращает количество тиков умноженное на speed, что ускоряет/замедляет игру.
 */
@Mixin(RenderTickCounter.Dynamic.class)
public abstract class TimerMixin {

    @Shadow private float tickDelta;
    private long lastFrameTime = 0;
    private boolean firstFrame = true;
    private boolean wasEnabled = false;

    @Inject(method = "beginRenderTick(J)I", at = @At("HEAD"), cancellable = true)
    private void onBeginRenderTick(long timeMillis, CallbackInfoReturnable<Integer> cir) {
        if (OneFizzMod.modules == null) return;
        var timer = OneFizzMod.modules.getTimer();
        var bypass = OneFizzMod.modules.getTimerBypass();
        boolean enabled = timer.isEnabled() || bypass.isEnabled();
        if (!enabled) { wasEnabled = false; return; }

        if (!wasEnabled) {
            lastFrameTime = timeMillis;
            firstFrame = true;
            wasEnabled = true;
        }

        if (firstFrame) {
            lastFrameTime = timeMillis;
            firstFrame = false;
            return;
        }

        // TimerBypass имеет приоритет если включён (обходит detection)
        float speed = bypass.isEnabled() ? bypass.getMultiplier() : timer.speed;

        float msPerTick = 50.0f / speed;
        long elapsed = timeMillis - lastFrameTime;
        lastFrameTime = timeMillis;

        float ticks = elapsed / msPerTick;
        int fullTicks = (int) ticks;
        tickDelta = ticks - fullTicks;

        cir.setReturnValue(fullTicks);
    }
}
