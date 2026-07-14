package com.onefizz.mixin;

import com.onefizz.OneFizzMod;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * FastPlace — каждый клиентский тик override'им itemUseCooldown
 * до значения из настроек модуля (0 = мгновенно).
 */
@Mixin(MinecraftClient.class)
public class FastPlaceMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTickHead(CallbackInfo ci) {
        if (OneFizzMod.modules == null) return;
        var fp = OneFizzMod.modules.getFastPlace();
        if (!fp.isEnabled()) return;

        MinecraftClient self = (MinecraftClient)(Object)this;
        ((MinecraftClientAccessor) self).setItemUseCooldown(fp.delay);
    }
}
