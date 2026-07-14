package com.onefizz.mixin;

import com.onefizz.modules.Perspective;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class PerspectiveCameraMixin {

    @Shadow private float yaw;
    @Shadow private float pitch;

    @Inject(method = "update", at = @At("TAIL"))
    private void onCameraUpdate(CallbackInfo ci) {
        if (Perspective.isActive()) {
            this.yaw = Perspective.cameraYaw;
            this.pitch = Perspective.cameraPitch;
        }
    }
}
