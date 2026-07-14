package com.onefizz;

/**
 * RotationManager — singleton для silent rotation.
 *
 * Когда модуль вызывает {@link #setOverride(float, float)}, следующие исходящие
 * PlayerMoveC2SPacket будут отправлены с указанными yaw/pitch вместо ванильных.
 * Камера игрока при этом НЕ меняется.
 *
 * Override автоматически сбрасывается через {@link #TIMEOUT_MS} миллисекунд,
 * если модуль не подтверждает его повторно.
 */
public final class RotationManager {

    private static final long TIMEOUT_MS = 100;

    private static volatile float yaw;
    private static volatile float pitch;
    private static volatile long lastSet = 0;

    public static void setOverride(float y, float p) {
        yaw = y;
        pitch = p;
        lastSet = System.currentTimeMillis();
    }

    public static void clear() {
        lastSet = 0;
    }

    public static boolean isActive() {
        return System.currentTimeMillis() - lastSet <= TIMEOUT_MS;
    }

    public static float getYaw()   { return yaw; }
    public static float getPitch() { return pitch; }
}
