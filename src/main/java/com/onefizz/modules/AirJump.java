package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.Setting;
import net.minecraft.client.MinecraftClient;

public class AirJump extends Module {

    @Setting(name = "Доп. прыжки", min = 1f, max = 5f) public int extraJumps = 2;
    @Setting(name = "Высота прыжка", min = 0.2f, max = 0.6f) public float jumpHeight = 0.42f;

    private int jumpsLeft = 0;
    private boolean wasJumpPressed = false;

    public AirJump() { super("AirJump", "Прыжки в воздухе без опоры"); }

    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null) return;

        if (mc.player.isOnGround() || mc.player.isClimbing()) {
            jumpsLeft = extraJumps;
            wasJumpPressed = mc.options.jumpKey.isPressed();
            return;
        }

        boolean jumpPressed = mc.options.jumpKey.isPressed();

        if (jumpsLeft > 0 && jumpPressed && !wasJumpPressed) {
            mc.player.setVelocity(mc.player.getVelocity().x, jumpHeight, mc.player.getVelocity().z);
            jumpsLeft--;
            mc.player.fallDistance = 0f;
        }

        wasJumpPressed = jumpPressed;
    }

    @Override
    protected void onDisable() {
        jumpsLeft = 0;
        wasJumpPressed = false;
    }
}
