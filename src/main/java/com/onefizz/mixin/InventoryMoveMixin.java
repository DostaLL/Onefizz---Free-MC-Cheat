package com.onefizz.mixin;

import com.onefizz.OneFizzMod;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class InventoryMoveMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(boolean slowDown, float f, CallbackInfo ci) {
        if (OneFizzMod.modules == null) return;
        if (!OneFizzMod.modules.getInventoryMove().isEnabled()) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.currentScreen == null) return;

        long w = mc.getWindow().getHandle();
        KeyboardInput self = (KeyboardInput)(Object)this;

        self.pressingForward  = isDown(w, mc.options.forwardKey);
        self.pressingBack     = isDown(w, mc.options.backKey);
        self.pressingLeft     = isDown(w, mc.options.leftKey);
        self.pressingRight    = isDown(w, mc.options.rightKey);
        self.jumping          = isDown(w, mc.options.jumpKey);
        self.sneaking         = isDown(w, mc.options.sneakKey);

        // Recompute movement vector based on raw input
        self.movementForward  = (self.pressingForward ? 1f : 0f) - (self.pressingBack ? 1f : 0f);
        self.movementSideways = (self.pressingLeft   ? 1f : 0f) - (self.pressingRight ? 1f : 0f);
        if (slowDown) {
            self.movementForward  *= 0.3f;
            self.movementSideways *= 0.3f;
        }
    }

    private static boolean isDown(long window, KeyBinding kb) {
        InputUtil.Key key = KeyBindingHelper.getBoundKeyOf(kb);
        if (key.equals(InputUtil.UNKNOWN_KEY)) return false;
        if (key.getCategory() == InputUtil.Type.KEYSYM) {
            return InputUtil.isKeyPressed(window, key.getCode());
        }
        if (key.getCategory() == InputUtil.Type.MOUSE) {
            return GLFW.glfwGetMouseButton(window, key.getCode()) == GLFW.GLFW_PRESS;
        }
        return false;
    }
}
