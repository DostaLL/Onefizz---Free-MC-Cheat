package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

/**
 * NoFall — отменяет урон от падения.
 *
 * GROUND_SPOOF: GroundSpoofMixin подменяет onGround=true в пакетах,
 *   но ТОЛЬКО один раз когда fallDistance > minFallDistance, потом ждёт сброса.
 *
 * PACKET_SPOOF: отправляет отдельный пакет позиции с Y-0.0001 и onGround=true
 *   один раз при fallDistance > minFallDistance. Не трогает обычные пакеты.
 */
public class NoFall extends Module {

    public enum Mode { GROUND_SPOOF, PACKET_SPOOF }

    @Setting
    public Mode mode = Mode.PACKET_SPOOF;

    @Setting(min = 1f, max = 10f)
    public float minFallDistance = 3f;

    // Внутреннее состояние — отправили ли уже спуф для текущего падения
    private boolean spoofSent = false;
    private float lastFallDistance = 0f;

    public NoFall() { super("NoFall", "Защита от урона при падении"); }

    /**
     * Вызывается из GroundSpoofMixin — нужно ли подменять onGround в этом пакете.
     * Только для GROUND_SPOOF режима, и только 1 раз за падение.
     */
    public boolean shouldSpoofGround() {
        if (!isEnabled() || mode != Mode.GROUND_SPOOF) return false;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return false;

        float fd = mc.player.fallDistance;

        // Падение закончилось — сбрасываем флаг
        if (fd < 0.5f && lastFallDistance >= minFallDistance) {
            spoofSent = false;
        }
        lastFallDistance = fd;

        // Спуфим только 1 раз когда fallDistance превысил порог
        if (fd >= minFallDistance && !spoofSent) {
            spoofSent = true;
            return true;
        }
        return false;
    }

    /**
     * Тик — для PACKET_SPOOF режима отправляем отдельный пакет.
     */
    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null) return;
        if (mode != Mode.PACKET_SPOOF) return;

        float fd = mc.player.fallDistance;

        // Падение закончилось — сбрасываем
        if (fd < 0.5f && lastFallDistance >= minFallDistance) {
            spoofSent = false;
        }
        lastFallDistance = fd;

        // Отправляем спуф-пакет 1 раз при достижении порога
        if (fd >= minFallDistance && !spoofSent) {
            spoofSent = true;
            double px = mc.player.getX();
            double py = mc.player.getY();
            double pz = mc.player.getZ();
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(
                px, py - 0.0001, pz,
                mc.player.getYaw(), mc.player.getPitch(), true
            ));
        }
    }

    @Override
    protected void onDisable() {
        spoofSent = false;
        lastFallDistance = 0f;
    }
}
