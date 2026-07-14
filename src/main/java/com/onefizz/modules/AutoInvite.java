package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;

import java.util.ArrayDeque;
import java.util.Deque;

public class AutoInvite extends Module {

    @Setting(name = "Задержка (мс)", min = 500f, max = 5000f)
    public int delayMs = 1500;

    private final Deque<String> queue = new ArrayDeque<>();
    private long lastSentMs = 0;
    private boolean loaded = false;

    public AutoInvite() { super("AutoInvite", "Автоприглашение игроков в команду"); }

    @Override
    protected void onEnable() { queue.clear(); loaded = false; }

    @Override
    protected void onDisable() { queue.clear(); loaded = false; }

    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null || mc.getNetworkHandler() == null) return;

        if (!loaded) {
            String myName = mc.player.getName().getString();
            for (PlayerListEntry entry : mc.getNetworkHandler().getPlayerList()) {
                String name = entry.getProfile().getName();
                if (name == null || name.isEmpty() || name.equals(myName)) continue;
                queue.add(name);
            }
            loaded = true;
        }

        if (queue.isEmpty()) { setEnabled(false); return; }

        long now = System.currentTimeMillis();
        if (now - lastSentMs < delayMs) return;

        String target = queue.poll();
        if (target != null) {
            mc.player.networkHandler.sendChatCommand("c invite " + target);
            lastSentMs = now;
        }
    }
}
