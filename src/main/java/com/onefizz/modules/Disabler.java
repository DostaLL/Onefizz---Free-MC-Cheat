package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

/**
 * Disabler — отправляет специально сформированные пакеты для отключения
 * проверок известных античитов.
 *
 * Эксплойты основаны на известных багах в обработке пакетов:
 * - GRIM_VELOCITY: spam ClientCommand START_SPRINTING — некоторые версии Grim
 *   сбрасывают velocity-tracking
 * - VULCAN_GROUND: чередование onGround=true с invalid Y — Vulcan может
 *   потерять prediction
 * - MATRIX_FLAG: спам PlayerInput с экстремальными значениями — некоторые
 *   версии Matrix впадают в "fail-open" режим
 * - GENERIC: combination всех — рассчитан на серверы со старыми/багованными АЧ
 *
 * Эффективность зависит от версии АЧ. На пропатченных серверах НЕ работает.
 */
public class Disabler extends Module {

    public enum Mode { GRIM_VELOCITY, VULCAN_GROUND, MATRIX_FLAG, GENERIC }

    @Setting(name = "Режим")
    public Mode mode = Mode.GENERIC;

    @Setting(name = "Интенсивность", min = 1f, max = 5f)
    public int intensity = 2;

    private int tickCounter = 0;

    public Disabler() { super("Disabler", "Отключение античит-проверок"); }

    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null || mc.player.networkHandler == null) return;
        tickCounter++;

        switch (mode) {
            case GRIM_VELOCITY -> grimVelocityExploit(mc);
            case VULCAN_GROUND -> vulcanGroundExploit(mc);
            case MATRIX_FLAG   -> matrixFlagExploit(mc);
            case GENERIC       -> {
                grimVelocityExploit(mc);
                if (tickCounter % 2 == 0) vulcanGroundExploit(mc);
                if (tickCounter % 3 == 0) matrixFlagExploit(mc);
            }
        }
    }

    /**
     * Grim Velocity Disabler: спам START/STOP SPRINTING пакетов.
     * Заставляет сервер постоянно пересчитывать sprint state, что в некоторых
     * версиях Grim ломает velocity tracking.
     */
    private void grimVelocityExploit(MinecraftClient mc) {
        var net = mc.player.networkHandler;
        for (int i = 0; i < intensity; i++) {
            net.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
            net.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
        }
    }

    /**
     * Vulcan Ground Disabler: отправка пакетов с быстро меняющимся onGround
     * и микро-Y offset. Vulcan ground-check pipeline иногда теряет state.
     */
    private void vulcanGroundExploit(MinecraftClient mc) {
        var net = mc.player.networkHandler;
        var p = mc.player;
        double x = p.getX(), y = p.getY(), z = p.getZ();
        float yaw = p.getYaw(), pitch = p.getPitch();

        for (int i = 0; i < intensity; i++) {
            // Чередуем onGround с микро-смещением Y
            net.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 1e-7, z, true));
            net.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false));
        }
    }

    /**
     * Matrix Flag Disabler: спам PlayerInput с разными jump/sneak комбинациями.
     * Matrix prediction иногда впадает в "uncertain" state.
     */
    private void matrixFlagExploit(MinecraftClient mc) {
        var net = mc.player.networkHandler;
        for (int i = 0; i < intensity; i++) {
            // Контрадикторные input: jump + sneak одновременно
            net.sendPacket(new PlayerInputC2SPacket(0f, 0f, true, true));
            net.sendPacket(new PlayerInputC2SPacket(0f, 0f, false, false));
        }
    }
}
