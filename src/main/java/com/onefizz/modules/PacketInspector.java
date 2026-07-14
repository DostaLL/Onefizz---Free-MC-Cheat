package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.PacketLogger;
import com.onefizz.RenderUtils;
import com.onefizz.Setting;
import com.onefizz.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.concurrent.ConcurrentLinkedDeque;

public class PacketInspector extends Module {

    @Setting(name = "Velocity")
    public boolean logVelocity = true;
    @Setting(name = "Teleport")
    public boolean logTeleport = true;
    @Setting(name = "Explosion")
    public boolean logExplosion = true;
    @Setting(name = "EntityStatus")
    public boolean logEntityStatus = true;
    @Setting(name = "BlockUpdate")
    public boolean logBlockUpdate = false;
    @Setting(name = "GameState")
    public boolean logGameState = false;

    @Setting(name = "HUD показ")
    public boolean showHUD = true;
    @Setting(name = "HUD X", min = 0f, max = 1000f)
    public int hudX = 4;
    @Setting(name = "HUD Y", min = 0f, max = 1000f)
    public int hudY = 80;

    private static final int MAX_EVENTS = 20;
    private final ConcurrentLinkedDeque<PacketEvent> events = new ConcurrentLinkedDeque<>();
    private long lastKeepAlive = 0;
    private int packetCount = 0;
    private int velocityCount = 0;
    private int teleportCount = 0;
    private int flagCount = 0;
    private long resetTime = System.currentTimeMillis();

    // Per-second rate
    private int ratePackets = 0;
    private int rateVelocity = 0;
    private int rateTeleport = 0;
    private long rateStart = System.currentTimeMillis();

    private static class PacketEvent {
        final String type;
        final String info;
        final int color;
        final long time;

        PacketEvent(String type, String info, int color) {
            this.type = type;
            this.info = info;
            this.color = color;
            this.time = System.currentTimeMillis();
        }
    }

    private static final int C_VELOCITY = 0xFF60A5FA;
    private static final int C_TELEPORT = 0xFFFBBF24;
    private static final int C_EXPLOSION = 0xFFF87171;
    private static final int C_FLAG = 0xFFEF4444;
    private static final int C_BLOCK = 0xFF34D399;
    private static final int C_STATE = 0xFFA78BFA;
    private static final int C_PACKET = 0xFF94A3B8;
    private static final int C_KEEPALIVE = 0xFFF472B6;

    public PacketInspector() { super("PacketInspector", "Анализ серверных пакетов"); }

    @Override
    protected void onEnable() {
        PacketLogger.start();
        packetCount = 0;
        velocityCount = 0;
        teleportCount = 0;
        flagCount = 0;
        events.clear();
        resetTime = System.currentTimeMillis();
        rateStart = System.currentTimeMillis();
        ratePackets = 0;
        rateVelocity = 0;
        rateTeleport = 0;
        PacketLogger.log("SYSTEM", "PacketInspector ENABLED");
    }

    @Override
    protected void onDisable() {
        PacketLogger.log("SYSTEM", String.format(
            "PacketInspector DISABLED | packets=%d velocity=%d teleports=%d flags=%d",
            packetCount, velocityCount, teleportCount, flagCount));
        PacketLogger.stop();
    }

    private void push(String type, String info, int color) {
        events.addLast(new PacketEvent(type, info, color));
        if (events.size() > MAX_EVENTS) events.removeFirst();
        packetCount++;
        ratePackets++;
    }

    public void onVelocity(int entityId, double vx, double vy, double vz) {
        if (!isEnabled() || !logVelocity) return;
        velocityCount++;
        rateVelocity++;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && entityId == mc.player.getId()) {
            double speed = Math.sqrt(vx * vx + vz * vz);
            String info = String.format("SELF v=(%.4f,%.4f,%.4f) speed=%.4f", vx, vy, vz, speed);
            PacketLogger.logWithContext("VELOCITY", info, mc.player.getX(), mc.player.getY(), mc.player.getZ());
            push("VL", String.format("%.3f", speed), C_VELOCITY);
        } else {
            PacketLogger.log("VELOCITY", "entity=" + entityId + " v=(" + vx + "," + vy + "," + vz + ")");
        }
    }

    public void onTeleport(double x, double y, double z, float yaw, float pitch) {
        if (!isEnabled() || !logTeleport) return;
        teleportCount++;
        rateTeleport++;
        MinecraftClient mc = MinecraftClient.getInstance();
        double dx = 0, dy = 0, dz = 0;
        if (mc.player != null) {
            dx = x - mc.player.getX();
            dy = y - mc.player.getY();
            dz = z - mc.player.getZ();
        }
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        String info = String.format("dist=%.2f dY=%.2f", dist, dy);
        PacketLogger.log("TELEPORT", String.format(
            "to=(%.2f,%.2f,%.2f) delta=(%.2f,%.2f,%.2f) dist=%.2f",
            x, y, z, dx, dy, dz, dist));
        int color = dist > 0.5 ? C_TELEPORT : (dist > 0.05 ? 0xFFFDE68A : 0xFF94A3B8);
        push(dist > 0.5 ? "RB" : "TP", info, color);
    }

    public void onExplosion(double vx, double vy, double vz) {
        if (!isEnabled() || !logExplosion) return;
        double speed = Math.sqrt(vx * vx + vy * vy + vz * vz);
        PacketLogger.log("EXPLOSION", String.format("vel=(%.4f,%.4f,%.4f) speed=%.4f", vx, vy, vz, speed));
        push("EXP", String.format("%.3f", speed), C_EXPLOSION);
    }

    public void onEntityStatus(int entityId, byte status) {
        if (!isEnabled() || !logEntityStatus) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        boolean self = mc.player != null && entityId == mc.player.getId();
        String ctx = self ? "SELF" : "eid=" + entityId;
        String desc = describeStatus(status);
        PacketLogger.log("ENTITY_STATUS", ctx + " status=" + status + " (" + desc + ")");
        if (status == 2 || status == 45 || status == 46 || status == 47) {
            flagCount++;
            push("FLG", desc, C_FLAG);
        } else {
            push("ES", desc, C_FLAG);
        }
    }

    public void onBlockUpdate(int x, int y, int z, int blockState) {
        if (!isEnabled() || !logBlockUpdate) return;
        PacketLogger.log("BLOCK_UPDATE", String.format("pos=(%d,%d,%d) state=%d", x, y, z, blockState));
        push("BLK", String.format("%d,%d,%d", x, y, z), C_BLOCK);
    }

    public void onGameStateChange(int reason, float value) {
        if (!isEnabled() || !logGameState) return;
        String desc = describeGameState(reason, value);
        PacketLogger.log("GAME_STATE", "reason=" + reason + " val=" + value + " (" + desc + ")");
        push("GS", desc, C_STATE);
    }

    public void onKeepAlive(long id) {
        if (!isEnabled()) return;
        long now = System.currentTimeMillis();
        long delta = lastKeepAlive > 0 ? now - lastKeepAlive : 0;
        lastKeepAlive = now;
        PacketLogger.log("KEEPALIVE", "id=" + id + " interval=" + delta + "ms");
    }

    public void onAnyPacket(String packetName) {
        if (!isEnabled()) return;
        PacketLogger.log("PACKET", packetName);
    }

    private String describeStatus(byte s) {
        switch (s) {
            case 2: return "HURT";
            case 3: return "DEATH";
            case 22: return "SHIELD_BREAK";
            case 31: return "TOTEM";
            case 35: return "THORNS";
            case 44: return "SWEEP";
            case 45: return "FLAG_45";
            case 46: return "FLAG_46";
            case 47: return "FLAG_47";
            default: return "s" + s;
        }
    }

    private String describeGameState(int reason, float value) {
        switch (reason) {
            case 0: return "NoRespawnBlock";
            case 1: return "RainLevel=" + value;
            case 2: return "Gamemode=" + (int) value;
            case 3: return "GameEnd";
            case 4: return "Demo";
            case 5: return "ArrowHit";
            case 6: return "Fog=" + value;
            case 7: return "Weather=" + (int) value;
            case 10: return "WorldAge=" + (long) value;
            case 11: return "Thunder=" + value;
            default: return "r" + reason;
        }
    }

    // ── HUD ─────────────────────────────────────────────────────────────────────

    public void onRender(DrawContext ctx) {
        if (!isEnabled() || !showHUD) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        // Reset per-second counters every second
        long now = System.currentTimeMillis();
        if (now - rateStart > 1000) {
            ratePackets = 0;
            rateVelocity = 0;
            rateTeleport = 0;
            rateStart = now;
        }

        int x = hudX;
        int y = hudY;
        int w = 200;
        int lineH = 10;
        int pad = 4;

        // Remove expired events (>5 seconds old)
        PacketEvent first;
        while ((first = events.peekFirst()) != null && now - first.time > 5000)
            events.removeFirst();

        int totalH = pad + lineH + 2 + pad;
        for (PacketEvent e : events) totalH += lineH;

        // Background
        RenderUtils.roundedRect(ctx, x, y, x + w, y + totalH, 4, 0xBB0E0A1A);
        RenderUtils.roundedOutline(ctx, x, y, x + w, y + totalH, 4, 0xFF1E1530);

        int ty = y + pad;
        ctx.drawText(mc.textRenderer, Text.literal(String.format(
            "PI | pkt=%d vel=%d tp=%d flg=%d | %.1f/s",
            packetCount, velocityCount, teleportCount, flagCount, ratePackets
        )), x + pad, ty, 0xFFEDE9FE, false);
        ty += lineH + 2;

        // Events list
        for (PacketEvent e : events) {
            long age = now - e.time;
            int alpha = Math.max(80, 255 - (int) (age * 255 / 5000));
            int col = (alpha << 24) | (e.color & 0x00FFFFFF);
            String label = "[" + e.type + "] " + e.info;
            ctx.drawText(mc.textRenderer, Text.literal(label), x + pad, ty, col, false);
            ty += lineH;
        }
    }
}
