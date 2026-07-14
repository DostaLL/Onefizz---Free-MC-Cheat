package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.Setting;
import net.minecraft.client.MinecraftClient;

public class NoHunger extends Module {

    @Setting(name = "Порог голода", min = 1f, max = 10f)
    public int hungerThreshold = 6;

    public NoHunger() { super("NoHunger", "Отключение траты голода"); }

    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null) return;
        if (mc.player.getHungerManager().getFoodLevel() < hungerThreshold) {
            mc.player.setSprinting(false);
        }
    }
}
