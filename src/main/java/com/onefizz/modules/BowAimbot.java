package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.OneFizzMod;
import com.onefizz.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;

public class BowAimbot extends Module {

    @Setting(name = "Дальность", min = 10f, max = 120f)
    public float range = 60f;

    @Setting(name = "Скорость наводки", min = 1f, max = 10f)
    public float aimSpeed = 5f;

    @Setting(name = "Угол обзора", min = 10f, max = 180f)
    public float fov = 90f;

    @Setting(name = "Упреждение")
    public boolean leadTarget = true;

    private static final double GRAVITY = 0.05;
    private static final double DRAG = 0.99;

    private LivingEntity cachedTarget = null;
    private int cacheTimer = 0;

    public BowAimbot() { super("BowAimbot", "Автонаводка лука и арбалета"); }

    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null || mc.world == null) return;

        var mainHand = mc.player.getMainHandStack();
        var offHand  = mc.player.getOffHandStack();
        boolean holdingBow = mainHand.getItem() == Items.BOW || offHand.getItem() == Items.BOW;
        if (!holdingBow) return;

        int useTicks = mc.player.getItemUseTime();
        if (useTicks <= 0) return;

        float charge = BowItem.getPullProgress(useTicks);
        if (charge < 0.1f) return;

        double arrowSpeed = charge * 3.0;

        // Re-target every 5 ticks for performance
        cacheTimer++;
        if (cacheTimer >= 5 || cachedTarget == null || cachedTarget.isDead()) {
            cachedTarget = findTarget(mc);
            cacheTimer = 0;
        }
        if (cachedTarget == null) return;

        Vec3d eyes = mc.player.getEyePos();
        Vec3d targetPos = getLeadPos(cachedTarget, eyes, arrowSpeed);

        double dx = targetPos.x - eyes.x;
        double dz = targetPos.z - eyes.z;
        double dy = targetPos.y - eyes.y;
        double horizDist = Math.sqrt(dx * dx + dz * dz);

        // FOV check
        float requiredYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float yawDiff = Math.abs(MathHelper.wrapDegrees(mc.player.getYaw() - requiredYaw));
        if (yawDiff > fov) return;

        float requiredPitch = calcPitch(eyes, targetPos, arrowSpeed);
        if (Float.isNaN(requiredPitch)) return;

        float t = aimSpeed / 10f;
        float newYaw = lerpAngle(mc.player.getYaw(), requiredYaw, t);
        float newPitch = MathHelper.lerp(t, mc.player.getPitch(), requiredPitch);

        mc.player.setYaw(newYaw);
        mc.player.setPitch(MathHelper.clamp(newPitch, -90f, 90f));
    }

    private float calcPitch(Vec3d from, Vec3d to, double speed) {
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double dz = to.z - from.z;
        double horizDist = Math.sqrt(dx * dx + dz * dz);

        double lo = -90.0, hi = 90.0;
        for (int i = 0; i < 64; i++) {
            double mid = (lo + hi) / 2.0;
            double pitchRad = Math.toRadians(mid);

            double vy = -Math.sin(pitchRad) * speed;
            double vh = Math.cos(pitchRad) * speed;

            double simH = 0, simY = 0;
            double cvh = vh, cvy = vy;

            boolean reached = false;
            for (int tick = 0; tick < 200; tick++) {
                double prevH = simH;
                double prevY = simY;
                simH += cvh;
                simY += cvy;
                cvy = cvy * DRAG - GRAVITY;
                cvh *= DRAG;

                if (simH >= horizDist || tick == 199) {
                    double frac = (simH - horizDist) / (simH - prevH);
                    double hitY = simY + (prevY - simY) * frac;
                    if (hitY > dy) {
                        lo = mid;
                    } else {
                        hi = mid;
                    }
                    reached = true;
                    break;
                }
            }
            if (!reached) return Float.NaN;
        }

        return (float) ((lo + hi) / 2.0);
    }

    private Vec3d getLeadPos(LivingEntity target, Vec3d from, double arrowSpeed) {
        Vec3d base = target.getPos().add(0, target.getHeight() / 2.0, 0);
        if (!leadTarget) return base;

        // Iterative lead estimation (3 iterations for convergence)
        Vec3d vel = target.getVelocity();
        Vec3d pos = base;
        for (int iter = 0; iter < 3; iter++) {
            double dist = from.distanceTo(pos);
            double flightTicks = dist / arrowSpeed;
            pos = base.add(vel.multiply(flightTicks));
        }
        return pos;
    }

    private LivingEntity findTarget(MinecraftClient mc) {
        return mc.world.getEntitiesByClass(LivingEntity.class,
            mc.player.getBoundingBox().expand(range),
            e -> e != mc.player
              && !e.isDead()
              && !(e instanceof PlayerEntity p && OneFizzMod.modules.getKillAura().friends.contains(p.getName().getString()))
              && mc.player.distanceTo(e) <= range
        ).stream()
            .min(Comparator.comparingDouble(e -> angleTo(mc, e)))
            .orElse(null);
    }

    private double angleTo(MinecraftClient mc, LivingEntity e) {
        Vec3d eyes = mc.player.getEyePos();
        Vec3d dir = e.getPos().add(0, e.getHeight()/2, 0).subtract(eyes).normalize();
        float yaw = (float) Math.toDegrees(Math.atan2(-dir.x, dir.z));
        float pitch = (float) Math.toDegrees(-Math.asin(dir.y));
        float dYaw = Math.abs(MathHelper.wrapDegrees(mc.player.getYaw() - yaw));
        float dPitch = Math.abs(mc.player.getPitch() - pitch);
        return Math.sqrt(dYaw * dYaw + dPitch * dPitch);
    }

    private float lerpAngle(float from, float to, float t) {
        return from + MathHelper.wrapDegrees(to - from) * t;
    }
}
