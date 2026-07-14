package com.onefizz.mixin;

import com.onefizz.OneFizzMod;
import com.onefizz.RotationManager;
import com.onefizz.modules.Blink;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerMoveC2SPacket.class)
public abstract class RotationMixin {

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        try {
            PlayerMoveAccessor self = (PlayerMoveAccessor)(Object) this;
            if (RotationManager.isActive()) {
                self.setYawField(RotationManager.getYaw());
                self.setPitchField(RotationManager.getPitch());
            }
            if (OneFizzMod.modules != null) {
                Blink blink = OneFizzMod.modules.getBlink();
                if (blink.isBlinking()) {
                    self.setXField(blink.getSavedX());
                    self.setYField(blink.getSavedY());
                    self.setZField(blink.getSavedZ());
                }
            }
        } catch (Throwable ignored) {}
    }
}
