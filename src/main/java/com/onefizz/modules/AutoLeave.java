package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.Setting;
import net.minecraft.client.MinecraftClient;

public class AutoLeave extends Module {

    @Setting(name = "Порог HP", min = 1f, max = 10f) public float hpThreshold = 4f;
    @Setting(name = "Команда") public String command = "/hub";
    @Setting(name = "Задержка (сек)", min = 1f, max = 5f) public int delaySec = 2;

    private int leaveTimer = -1;

    public AutoLeave() { super("AutoLeave", "Автовыход при низком HP"); }

    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null || mc.world == null) return;

        float hp = mc.player.getHealth();

        if (hp <= hpThreshold) {
            if (leaveTimer == -1) {
                leaveTimer = delaySec * 20;
            }
            leaveTimer--;
            if (leaveTimer <= 0) {
                if (command.startsWith("/")) {
                    mc.player.networkHandler.sendChatCommand(command.substring(1));
                } else {
                    mc.player.networkHandler.sendChatMessage(command);
                }
                leaveTimer = -1;
            }
        } else {
            if (leaveTimer != -1) {
                mc.player.sendMessage(
                    net.minecraft.text.Text.literal("§a[AutoLeave] §fHP восстановлено, выход отменён"), true);
                leaveTimer = -1;
            }
        }
    }

    @Override
    protected void onDisable() {
        leaveTimer = -1;
    }
}
