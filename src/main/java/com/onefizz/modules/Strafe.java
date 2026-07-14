package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.OneFizzMod;
import com.onefizz.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class Strafe extends Module {

    @Setting(name = "Сила", min = 0.1f, max = 1.0f) public float strength = 1.0f;
    @Setting(name = "Лимит скорости", min = 0.1f, max = 0.6f) public float speedCap = 0.2873f;
    @Setting(name = "Только в бою") public boolean onlyInCombat = false;
    @Setting(name = "Авто-прыжок") public boolean autoJump = true;

    public Strafe() { super("Strafe", "Стрейф-увороты в бою"); }

    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null) return;
        var player = mc.player;

        if (onlyInCombat) {
            var ka = OneFizzMod.modules.getKillAura();
            if (!ka.isEnabled() || ka.getLockedTarget() == null) return;
        }

        boolean pressingW = mc.options.forwardKey.isPressed();
        boolean pressingS = mc.options.backKey.isPressed();
        boolean pressingA = mc.options.leftKey.isPressed();
        boolean pressingD = mc.options.rightKey.isPressed();
        if (!pressingW && !pressingS && !pressingA && !pressingD) return;

        if (autoJump && player.isOnGround() && pressingW) {
            player.jump();
        }

        Vec3d vel = player.getVelocity();
        double speed = Math.sqrt(vel.x * vel.x + vel.z * vel.z);

        if (speed > speedCap) return;

        float yawRad = (float) Math.toRadians(player.getYaw());
        double mx = 0, mz = 0;

        if (pressingW) { mx -= Math.sin(yawRad); mz += Math.cos(yawRad); }
        if (pressingS) { mx += Math.sin(yawRad); mz -= Math.cos(yawRad); }
        if (pressingA) { mx += Math.cos(yawRad); mz += Math.sin(yawRad); }
        if (pressingD) { mx -= Math.cos(yawRad); mz -= Math.sin(yawRad); }

        double len = Math.sqrt(mx * mx + mz * mz);
        if (len < 0.01) return;
        mx /= len;
        mz /= len;

        double targetSpeed = player.isOnGround() ? speedCap * 0.8 : speedCap;
        double newX = MathHelper.lerp(strength, vel.x, mx * targetSpeed);
        double newZ = MathHelper.lerp(strength, vel.z, mz * targetSpeed);

        double cap = Math.max(speed, targetSpeed);
        double newSpeed = Math.sqrt(newX * newX + newZ * newZ);
        if (newSpeed > cap) {
            newX = newX / newSpeed * cap;
            newZ = newZ / newSpeed * cap;
        }

        player.setVelocity(newX, vel.y, newZ);
    }
}
