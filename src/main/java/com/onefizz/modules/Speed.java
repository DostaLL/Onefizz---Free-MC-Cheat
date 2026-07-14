package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

/**
 * Speed — bunny-hop ускорение с обходом античитов.
 *
 * Техника: прыжок в момент приземления + strafe boost.
 * Скорость ограничена до 0.28-0.32 б/т (ванильный спринт-прыжок = 0.26).
 * Рандомизация: boost не каждый тик, случайный множитель.
 */
public class Speed extends Module {

    public enum Mode { VANILLA, MATRIX }

    @Setting(name = "Режим") public Mode mode = Mode.VANILLA;

    @Setting(name = "Множитель скорости", min = 1.0f, max = 3.0f)
    public float multiplier = 1.5f;

    private final Random random = new Random();
    private int airTicks = 0;
    private int groundTicks = 0;
    private boolean wasOnGround = false;

    public Speed() { super("Speed", "Ускоренное передвижение"); }

    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null) return;
        var p = mc.player;

        // Only work when player is moving
        if (p.input.movementForward == 0 && p.input.movementSideways == 0) {
            airTicks = 0;
            groundTicks = 0;
            wasOnGround = p.isOnGround();
            return;
        }

        // Auto-sprint
        p.setSprinting(true);

        if (p.isOnGround()) {
            groundTicks++;
            airTicks = 0;

            // Минимум 1 тик на земле перед следующим прыжком (антидетект)
            if (groundTicks >= 2) {
                p.jump();
                groundTicks = 0;
                // Strafe boost только при приземлении (не первый ground frame)
                if (!wasOnGround) {
                    applyBoost(p);
                }
            }
        } else {
            groundTicks = 0;
            airTicks++;
            // Boost только на 1-2 air tick с 60% шансом
            if (airTicks <= 2 && random.nextFloat() < 0.6f) {
                applyBoost(p);
            }
        }

        // Always cap speed
        capSpeed(p);
        wasOnGround = p.isOnGround();
    }

    private void applyBoost(net.minecraft.client.network.ClientPlayerEntity p) {
        float yaw = p.getYaw();
        double rad = Math.toRadians(yaw + 90);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);
        float mf = p.input.movementForward;
        float ms = p.input.movementSideways;

        // Small boost with randomization (0.02-0.04 range)
        double boost = (0.02 + random.nextDouble() * 0.02) * (multiplier - 1.0);
        Vec3d v = p.getVelocity();
        double vx = v.x + (cos * mf - sin * ms) * boost;
        double vz = v.z + (sin * mf + cos * ms) * boost;
        p.setVelocity(vx, v.y, vz);
    }

    private void capSpeed(net.minecraft.client.network.ClientPlayerEntity p) {
        Vec3d v = p.getVelocity();
        double horizLen = Math.sqrt(v.x * v.x + v.z * v.z);
        // Ванильный спринт-прыжок = 0.2873. Не превышаем.
        double maxH = 0.2873;
        if (horizLen > maxH) {
            double scale = maxH / horizLen;
            p.setVelocity(v.x * scale, v.y, v.z * scale);
        }
    }
}
