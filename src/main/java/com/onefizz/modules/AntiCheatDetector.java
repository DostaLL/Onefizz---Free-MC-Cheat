package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.ToastManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

/**
 * AntiCheatDetector — пассивный анализатор античита на сервере.
 *
 * Анализирует поведение сервера через PacketInspector хуки:
 * - Частота velocity пакетов → скорее Grim/Vulcan
 * - Большие teleport delta → классический rubberband (NCP/AAC)
 * - Маленькие teleport delta (<0.1) → prediction-based (Matrix/Grim 2)
 * - Отсутствие реакции → нет античита или очень слабый
 *
 * Результат показывает в ActionBar и в логе.
 */
public class AntiCheatDetector extends Module {

    public enum DetectedAC {
        UNKNOWN, NONE, NCP, AAC, GRIM_LEGACY, GRIM_PREDICTION, VULCAN, MATRIX, INTAVE, CUSTOM
    }

    private DetectedAC detected = DetectedAC.UNKNOWN;
    private int velocityCount = 0;
    private int largeTeleportCount = 0;
    private int smallTeleportCount = 0;
    private long lastDetectionMs = 0;
    private int sampleTicks = 0;

    public AntiCheatDetector() { super("AntiCheatDetector", "Анализ серверного античита"); }

    @Override
    protected void onEnable() {
        detected = DetectedAC.UNKNOWN;
        velocityCount = 0;
        largeTeleportCount = 0;
        smallTeleportCount = 0;
        sampleTicks = 0;
    }

    /** Вызывается из PacketInspectorMixin при velocity */
    public void onVelocity(int entityId, double vx, double vy, double vz) {
        if (!isEnabled()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || entityId != mc.player.getId()) return;
        velocityCount++;
    }

    /** Вызывается из PacketInspectorMixin при teleport */
    public void onTeleport(double x, double y, double z) {
        if (!isEnabled()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        double dx = x - mc.player.getX();
        double dy = y - mc.player.getY();
        double dz = z - mc.player.getZ();
        double dist = Math.sqrt(dx*dx + dy*dy + dz*dz);
        if (dist > 0.5) largeTeleportCount++;
        else if (dist > 0.001) smallTeleportCount++;
    }

    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null) return;
        sampleTicks++;

        // Анализируем каждые 100 тиков (5 секунд)
        if (sampleTicks < 100) return;
        sampleTicks = 0;

        DetectedAC newDetection = analyze();
        if (newDetection != detected) {
            detected = newDetection;
            lastDetectionMs = System.currentTimeMillis();
            ToastManager.INSTANCE.push("AC: " + detected.name(), true);
        }

        // Сбрасываем счётчики для следующего семпла
        velocityCount = 0;
        largeTeleportCount = 0;
        smallTeleportCount = 0;
    }

    private DetectedAC analyze() {
        // Нет рубербандов и velocity — скорее всего нет АЧ
        if (velocityCount == 0 && largeTeleportCount == 0 && smallTeleportCount == 0) {
            return DetectedAC.NONE;
        }

        // Много маленьких телепортов = prediction-based (Matrix custom, Grim 2)
        if (smallTeleportCount > 5 && largeTeleportCount < 3) {
            return DetectedAC.GRIM_PREDICTION;
        }

        // Большие рубербанды + много velocity = Grim/Vulcan
        if (largeTeleportCount > 5 && velocityCount > 10) {
            return DetectedAC.GRIM_LEGACY;
        }

        // Только большие рубербанды без velocity = NCP/AAC классика
        if (largeTeleportCount > 3 && velocityCount < 3) {
            return DetectedAC.NCP;
        }

        // Много velocity без рубербандов = Vulcan
        if (velocityCount > 15 && largeTeleportCount < 2) {
            return DetectedAC.VULCAN;
        }

        // Смешанное поведение — кастомный или Matrix
        if (smallTeleportCount > 0 && largeTeleportCount > 0) {
            return DetectedAC.MATRIX;
        }

        return DetectedAC.UNKNOWN;
    }

    public DetectedAC getDetected() { return detected; }
}
