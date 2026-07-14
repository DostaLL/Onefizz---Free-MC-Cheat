package com.onefizz.mixin;

import com.onefizz.OneFizzTitleScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin {

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void onInit(CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        // Перенаправляем на наш кастомный экран вместо ванильного
        if (!(mc.currentScreen instanceof OneFizzTitleScreen)) {
            mc.setScreen(new OneFizzTitleScreen());
            ci.cancel();
        }
    }
}
