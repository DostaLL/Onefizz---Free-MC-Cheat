package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;

import java.util.Random;

public class AntiAFK extends Module {

    public enum ActionType { SWING, SPRINT, SWING_SPRINT }

    @Setting(name = "Интервал (сек)", min = 5f, max = 120f) public int interval = 45;
    @Setting(name = "Тип действия") public ActionType actionType = ActionType.SWING_SPRINT;

    private final Random random = new Random();
    private long lastAction = 0;
    private boolean sprinting = false;

    public AntiAFK() { super("AntiAFK", "Анти-AFK: автоповорот и движение"); }

    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null || mc.world == null) return;

        long now = System.currentTimeMillis();
        if (now - lastAction < interval * 1000L) return;
        lastAction = now;

        switch (actionType) {
            case SWING -> {
                mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            }
            case SPRINT -> {
                // Чередуем START/STOP sprinting — безопасно и не вызывает лагбэк
                sprinting = !sprinting;
                mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player,
                    sprinting
                        ? ClientCommandC2SPacket.Mode.START_SPRINTING
                        : ClientCommandC2SPacket.Mode.STOP_SPRINTING));
            }
            case SWING_SPRINT -> {
                mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                sprinting = !sprinting;
                mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player,
                    sprinting
                        ? ClientCommandC2SPacket.Mode.START_SPRINTING
                        : ClientCommandC2SPacket.Mode.STOP_SPRINTING));
            }
        }
    }

    @Override
    protected void onDisable() {
        lastAction = 0;
        sprinting = false;
    }
}
