package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.OneFizzMod;
import com.onefizz.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

import java.util.ArrayDeque;

public class FakeLag extends Module {

    public enum LagMode { MOVE, ATTACK, BOTH }

    @Setting(name = "Задержка (мс)", min = 50f, max = 500f) public int delay = 150;
    @Setting public LagMode mode = LagMode.MOVE;

    private final ArrayDeque<PlayerMoveC2SPacket> buffer = new ArrayDeque<>();
    private long lastFlush = 0;

    public FakeLag() { super("FakeLag", "Искусственный лаг для обхода"); }

    public void onTick(MinecraftClient mc) {
        if (!isEnabled()) { flush(); return; }
        if (System.currentTimeMillis() - lastFlush >= delay) {
            flush();
        }
    }

    public void tryBuffer(PlayerMoveC2SPacket packet) {
        if (!isEnabled()) return;
        boolean kaActive = OneFizzMod.modules.getKillAura().isEnabled();

        switch (mode) {
            case MOVE -> buffer.add(packet);
            case ATTACK -> { if (kaActive) buffer.add(packet); }
            case BOTH -> {
                buffer.add(packet);
                if (kaActive) flush();
            }
        }
    }

    public void flush() {
        if (buffer.isEmpty()) return;
        var player = MinecraftClient.getInstance().player;
        if (player == null) { buffer.clear(); return; }

        PlayerMoveC2SPacket p;
        while ((p = buffer.poll()) != null) {
            player.networkHandler.sendPacket(p);
        }
        lastFlush = System.currentTimeMillis();
    }

    public void onDamage() {
        flush();
    }
}
