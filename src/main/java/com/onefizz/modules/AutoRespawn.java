package com.onefizz.modules;

import com.onefizz.Module;
import net.minecraft.client.MinecraftClient;

public class AutoRespawn extends Module {

    public AutoRespawn() { super("AutoRespawn", "Автоматическое возрождение"); }

    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null) return;
        if (mc.player.isDead()) {
            mc.player.requestRespawn();
        }
    }
}
