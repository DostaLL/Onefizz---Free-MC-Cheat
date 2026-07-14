package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.OneFizzMod;
import com.onefizz.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

public class TargetStrafe extends Module {

    public enum StrafeDirection { CLOCKWISE, COUNTER, AUTO }

    @Setting(name = "Скорость", min = 0.05f, max = 0.3f)
    public float speed = 0.2f;

    @Setting(name = "Направление")
    public StrafeDirection direction = StrafeDirection.AUTO;

    private float angle = 0;
    private int autoDir = 1;

    public TargetStrafe() { super("TargetStrafe", "Кружение вокруг цели KillAura"); }

    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null) return;

        KillAura ka = OneFizzMod.modules.getKillAura();
        if (!ka.isEnabled()) return;
        LivingEntity target = ka.getLockedTarget();
        if (target == null || target.isDead()) return;

        float radius = ka.reach - 0.5f;
        int dir = switch (direction) {
            case CLOCKWISE -> 1;
            case COUNTER -> -1;
            case AUTO -> autoDir;
        };
        if (direction == StrafeDirection.AUTO && mc.player.horizontalCollision) autoDir = -autoDir;

        angle += 0.15f * dir;

        double tx = target.getX() + Math.cos(angle) * radius;
        double tz = target.getZ() + Math.sin(angle) * radius;

        Vec3d move = new Vec3d(tx - mc.player.getX(), 0, tz - mc.player.getZ()).normalize().multiply(speed);
        Vec3d vel = mc.player.getVelocity();
        mc.player.setVelocity(vel.x + move.x, vel.y, vel.z + move.z);
    }
}
