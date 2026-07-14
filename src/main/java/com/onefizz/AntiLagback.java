package com.onefizz;

import net.minecraft.client.MinecraftClient;

/**
 * AntiLagback — глобальный детектор рубербандов.
 * 
 * Когда сервер телепортирует игрока (рубербанд) — устанавливает
 * флаг suppressUntilTick. Movement модули (Scaffold, AutoBridge, Flight)
 * проверяют этот флаг и приостанавливаются.
 * 
 * Вызывается из PacketInspectorMixin при teleport пакете.
 */
public final class AntiLagback {

    private static volatile long suppressUntilMs = 0;
    private static volatile int rubberbandCount = 0;
    private static volatile long lastRubberbandMs = 0;

    /** Вызывается из mixin при получении teleport пакета */
    public static void onRubberband(double dist) {
        long now = System.currentTimeMillis();
        // Только если реальный рубербанд (>0.5 блока)
        if (dist < 0.5) return;

        // Если рубербанды происходят часто — увеличиваем suppress time
        if (now - lastRubberbandMs < 2000) {
            rubberbandCount++;
        } else {
            rubberbandCount = 1;
        }
        lastRubberbandMs = now;

        // Базовая пауза 500мс, +200мс за каждый последующий рубербанд (макс 2с)
        long pause = Math.min(2000, 500 + rubberbandCount * 200L);
        suppressUntilMs = now + pause;
    }

    /** true если movement модули должны временно остановиться */
    public static boolean isSuppressed() {
        return System.currentTimeMillis() < suppressUntilMs;
    }

    /** Сколько мс до конца suppress */
    public static long suppressRemaining() {
        long rem = suppressUntilMs - System.currentTimeMillis();
        return Math.max(0, rem);
    }

    /** Принудительно сбрасывает suppress */
    public static void reset() {
        suppressUntilMs = 0;
        rubberbandCount = 0;
    }
}
