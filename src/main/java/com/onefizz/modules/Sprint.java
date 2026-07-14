package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.Setting;
import net.minecraft.client.MinecraftClient;

public class Sprint extends Module {

    @Setting(name = "Во все стороны")
    public boolean omnidirectional = true; // спринт во все стороны (не только вперёд)

    @Setting(name = "Не сбрасывать в воздухе")
    public boolean keepInAir = true; // не сбрасывать спринт в воздухе

    public Sprint() { super("Sprint", "Автоматический бег"); }

    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null) return;
        var p = mc.player;
        if (p.isTouchingWater() && !p.getAbilities().flying) return;
        if (p.getHungerManager().getFoodLevel() <= 6 && !p.getAbilities().flying) return;
        if (p.isSneaking() || p.isUsingItem()) return;

        boolean moving = mc.options.forwardKey.isPressed()
                       || mc.options.backKey.isPressed()
                       || mc.options.leftKey.isPressed()
                       || mc.options.rightKey.isPressed();
        if (!moving) return;

        // Default Minecraft requires only forward key for sprint
        if (!omnidirectional && !mc.options.forwardKey.isPressed()) return;

        if (!p.isOnGround() && !keepInAir) return;

        p.setSprinting(true);
    }
}
