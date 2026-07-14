package com.onefizz.mixin;

import com.onefizz.OneFizzMod;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Слушает нажатия клавиш и тоглит модули привязанные к ним.
 * Срабатывает только когда нет открытого экрана (чтобы не мешать в GUI).
 */
@Mixin(Keyboard.class)
public class KeyBindMixin {

    @Inject(method = "onKey", at = @At("HEAD"))
    private void onKey(long window, int key, int scancode, int action, int modifiers,
                        CallbackInfo ci) {
        try {
            if (action != GLFW.GLFW_PRESS) return;
            if (OneFizzMod.modules == null) return;

            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc == null) return;
            // Don't toggle while typing in a screen
            if (mc.currentScreen != null) return;

            for (var m : OneFizzMod.modules.getAll()) {
                if (m.getKeyBind() == key) {
                    m.toggle();
                    return;
                }
            }
        } catch (Throwable ignored) {}
    }
}
