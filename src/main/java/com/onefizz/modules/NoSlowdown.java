package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;

/**
 * NoSlowdown — отменяет замедление при использовании предметов.
 * Для обхода Grim: отправляет sprint=true пакет во время использования предмета.
 * НЕ влияет на soul sand / honey (только items).
 */
public class NoSlowdown extends Module {

    @Setting
    public boolean items = true;

    @Setting
    public boolean grimBypass = true; // отправлять sprint пакет

    public NoSlowdown() { super("NoSlowdown", "Отключение замедления от еды/лука"); }

    /**
     * Вызывается каждый тик — отправляет sprint пакет для обхода Grim.
     */
    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null) return;
        if (!items || !grimBypass) return;

        // Только когда игрок реально использует предмет и двигается
        if (mc.player.isUsingItem() && mc.player.input.movementForward > 0) {
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(
                mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING
            ));
        }
    }
}
