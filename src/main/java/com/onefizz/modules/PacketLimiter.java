package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.Setting;

/**
 * PacketLimiter — ограничивает количество исходящих пакетов в тик.
 *
 * Некоторые античиты считают packet rate и кикают за превышение.
 * Этот модуль сбрасывает излишние пакеты позиции через mixin.
 *
 * Сам по себе модуль не делает ничего активного — он только
 * предоставляет shouldLimit() и хранит state. Реальная блокировка
 * пакетов происходит в PacketLimiterMixin.
 */
public class PacketLimiter extends Module {

    @Setting(name = "Лимит пакетов/тик", min = 1f, max = 20f)
    public int maxPackets = 8;

    @Setting(name = "Счётчик в чат")
    public boolean debug = false;

    private int packetsSentThisTick = 0;
    private int packetsDroppedThisTick = 0;
    private long lastTickMs = 0;

    public PacketLimiter() { super("PacketLimiter", "Лимит исходящих пакетов"); }

    /**
     * Вызывается из mixin при попытке отправить movement пакет.
     * @return true если пакет должен быть отброшен
     */
    public boolean shouldDrop() {
        if (!isEnabled()) return false;

        // Сбрасываем счётчик каждый тик (50ms)
        long now = System.currentTimeMillis();
        if (now - lastTickMs >= 50) {
            if (debug && packetsDroppedThisTick > 0) {
                System.out.println("[OneFizz] PacketLimiter: dropped " + packetsDroppedThisTick + " packets");
            }
            packetsSentThisTick = 0;
            packetsDroppedThisTick = 0;
            lastTickMs = now;
        }

        if (packetsSentThisTick >= maxPackets) {
            packetsDroppedThisTick++;
            return true;
        }
        packetsSentThisTick++;
        return false;
    }

    public int getSentThisTick() { return packetsSentThisTick; }
    public int getDroppedThisTick() { return packetsDroppedThisTick; }
}
