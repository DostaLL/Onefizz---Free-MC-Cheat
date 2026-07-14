package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.Setting;
import net.minecraft.client.MinecraftClient;

public class Spider extends Module {

    @Setting(name = "Скорость подъёма", min = 0.1f, max = 0.3f) public float climbSpeed = 0.2f;

    public Spider() { super("Spider", "Лазание по вертикальным стенам"); }

    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null || mc.world == null) return;

        boolean pressingForward = mc.options.forwardKey.isPressed();
        boolean sneaking = mc.options.sneakKey.isPressed();

        if (!pressingForward || sneaking) return;

        // horizontalCollision = true когда игрок упирается в стену
        if (mc.player.horizontalCollision) {
            mc.player.setVelocity(mc.player.getVelocity().x, climbSpeed, mc.player.getVelocity().z);
            mc.player.fallDistance = 0f;
        }
    }
}
