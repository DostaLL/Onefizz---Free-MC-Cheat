package com.onefizz.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * BlurGUIMixin — убирает тёмный overlay в GUI (сундуки, печки, инвентарь).
 * Оставляет blur фон если он есть в игре.
 */
@Mixin(Screen.class)
public class BlurGUIMixin {

    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    private void onRenderBackground(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        // Пропускаем только для ванильных GUI (сундуки, печки, инвентарь)
        // OneFizzScreen обрабатывается отдельно
        Screen self = (Screen) (Object) this;
        String name = self.getClass().getName();
        if (name.contains("OneFizzScreen")) return;
        if (name.contains("net.minecraft.client.gui.screen")) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.world != null) {
                // Пробуем включить blur шейдер через reflection
                try {
                    java.lang.reflect.Method m = mc.gameRenderer.getClass().getDeclaredMethod("renderBlur", float.class);
                    m.setAccessible(true);
                    m.invoke(mc.gameRenderer, delta);
                } catch (Exception ignored) {}
                // Не рисуем тёмный прямоугольник — оставляем blur или прозрачный мир
                ci.cancel();
            }
        }
    }
}
