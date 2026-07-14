package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

/**
 * Phase — попытка прохода сквозь тонкие блоки через packet timing.
 *
 * Идея: отправляем 2 пакета позиции подряд — один с позицией ДО стены,
 * второй С позицией ЗА стеной. Если сервер обрабатывает пакеты в одном
 * тике без collision check — игрок пройдёт сквозь.
 *
 * Работает только на серверах БЕЗ движкового collision check
 * (Vanilla Paper без plugins, старые сервера). На Grim/Matrix НЕ работает.
 *
 * ВАЖНО: модуль активируется только при движении вперёд + Sneak.
 * Это safety чтобы не активировалось случайно.
 */
public class Phase extends Module {

    @Setting(name = "Дальность фазы", min = 0.5f, max = 3.0f)
    public float phaseDistance = 1.0f;

    @Setting(name = "Только при Sneak")
    public boolean onlyWithSneak = true;

    public Phase() { super("Phase", "Прохождение сквозь стены и двери"); }

    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null || mc.player.networkHandler == null) return;

        var opts = mc.options;
        if (!opts.forwardKey.isPressed()) return;
        if (onlyWithSneak && !opts.sneakKey.isPressed()) return;

        var player = mc.player;
        float yaw = (float) Math.toRadians(player.getYaw());
        double dx = -Math.sin(yaw) * phaseDistance;
        double dz = Math.cos(yaw) * phaseDistance;

        double px = player.getX(), py = player.getY(), pz = player.getZ();

        // Серия пакетов с увеличивающимся offset — пытаемся "проскользнуть"
        // через коллизию пока сервер не успел проверить
        int steps = 4;
        for (int i = 1; i <= steps; i++) {
            double t = (double) i / steps;
            player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                px + dx * t, py, pz + dz * t, false));
        }

        // Финальная позиция с onGround=true чтобы зафиксировать
        player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
            px + dx, py, pz + dz, true));

        // Локально телепортируем
        player.setPosition(px + dx, py, pz + dz);
    }
}
