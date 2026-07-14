package com.onefizz.mixin;

import com.onefizz.modules.Perspective;
import net.minecraft.client.Mouse;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Mouse.class)
public class PerspectiveMouseMixin {

    @Redirect(
        method = "updateMouse",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;changeLookDirection(DD)V"
        )
    )
    private void redirectChangeLookDirection(Entity player, double cursorDeltaX, double cursorDeltaY) {
        if (Perspective.isActive()) {
            Perspective.onMouseDelta(cursorDeltaX, cursorDeltaY);
        } else {
            player.changeLookDirection(cursorDeltaX, cursorDeltaY);
        }
    }
}
