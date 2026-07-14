package com.onefizz.mixin;

import com.onefizz.OneFizzMod;
import com.onefizz.modules.Jesus;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.tag.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * JesusMixin — handles fluid interaction for Jesus module.
 * SOLID mode: blocks fluid push entirely (acts like solid block).
 * MOTION mode: zeroes Y velocity after fluid physics.
 */
@Mixin(Entity.class)
public class JesusMixin {

    @Inject(method = "updateMovementInFluid", at = @At("HEAD"), cancellable = true)
    private void onUpdateMovementInFluidHead(TagKey<Fluid> tag, double speed, CallbackInfoReturnable<Boolean> cir) {
        if (OneFizzMod.modules == null) return;
        Jesus jesus = OneFizzMod.modules.getJesus();
        if (jesus == null || !jesus.isSolidMode()) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        Entity self = (Entity)(Object)this;
        if (self != mc.player) return;

        // In SOLID mode, skip fluid movement entirely if not sneaking
        if (!mc.options.sneakKey.isPressed()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "updateMovementInFluid", at = @At("TAIL"))
    private void onUpdateMovementInFluid(TagKey<Fluid> tag, double speed, CallbackInfoReturnable<Boolean> cir) {
        if (OneFizzMod.modules == null) return;
        Jesus jesus = OneFizzMod.modules.getJesus();
        if (jesus == null || !jesus.isEnabled()) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        Entity self = (Entity)(Object)this;
        if (self != mc.player) return;
        if (!cir.getReturnValue()) return;

        // For MOTION mode: zero Y after fluid physics ran
        if (jesus.mode == Jesus.Mode.MOTION && !mc.options.sneakKey.isPressed()) {
            mc.player.setVelocity(mc.player.getVelocity().x, 0, mc.player.getVelocity().z);
        }
    }
}
