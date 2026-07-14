package com.onefizz.modules;

import com.onefizz.AntiLagback;
import com.onefizz.Module;
import com.onefizz.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

public class Flight extends Module {

    public enum Mode { VANILLA, GRIM_LEGACY, MATRIX, VULCAN, INTave }

    @Setting(name = "Режим")
    public Mode mode = Mode.GRIM_LEGACY;

    @Setting(name = "Скорость", min = 0.01f, max = 5f)
    public float speed = 0.28f;
    @Setting(name = "Вверх", min = 0.01f, max = 5f)
    public float speedUp = 0.42f;
    @Setting(name = "Вниз", min = 0.01f, max = 5f)
    public float speedDown = 0.1f;

    private int tick;
    private int phaseOffset;
    private int grimDelay;
    private int grimNext;
    private int rollbackSkip;
    private final Random random = new Random();

    public Flight() { super("Flight", "Полет с обходом античитов"); }

    @Override
    protected void onEnable() {
        tick = 0;
        phaseOffset = random.nextInt(10);
        grimDelay = 0;
        grimNext = 4 + random.nextInt(5);
        rollbackSkip = 0;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        if (mode == Mode.VANILLA) {
            var ab = mc.player.getAbilities();
            ab.allowFlying = true;
            ab.flying = true;
        }
    }

    @Override
    protected void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        var ab = mc.player.getAbilities();
        ab.allowFlying = false;
        ab.flying = false;
        ab.setFlySpeed(0.05f);
    }

    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null) return;
        if (AntiLagback.isSuppressed()) { return; }

        if (rollbackSkip > 0) { rollbackSkip--; return; }
        if (wasRollbacked(mc)) { rollbackSkip = 2; return; }

        switch (mode) {
            case VANILLA     -> tickVanilla(mc);
            case GRIM_LEGACY -> tickGrimLegacy(mc);
            case MATRIX      -> tickMatrix(mc);
            case VULCAN      -> tickVulcan(mc);
            case INTave      -> tickIntave(mc);
        }
    }

    private boolean wasRollbacked(MinecraftClient mc) {
        var p = mc.player;
        double dx = p.getX() - p.lastRenderX;
        double dz = p.getZ() - p.lastRenderZ;
        double dist = Math.sqrt(dx * dx + dz * dz);
        return dist > 2.0;
    }

    private void tickVanilla(MinecraftClient mc) {
        var ab = mc.player.getAbilities();
        ab.allowFlying = true;
        ab.flying = true;
        ab.setFlySpeed(speed / 2f);
        tick++;
        if (tick >= 40) { tick = 0;
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true)); }
    }

    private void tickGrimLegacy(MinecraftClient mc) {
        var player = mc.player;
        var opts = mc.options;
        player.getAbilities().allowFlying = false;
        player.getAbilities().flying = false;

        Vec3d vel = moveVec(player.getYaw(), opts);
        double mx = vel.x, mz = vel.z;
        double h = Math.sqrt(mx * mx + mz * mz);
        double cap = Math.min(speed, 0.19);
        if (h > cap) { mx = mx / h * cap; mz = mz / h * cap; }
        double vy = vert(opts);

        tick++;
        grimDelay++;
        boolean spoof = grimDelay >= grimNext;
        if (spoof) { grimDelay = 0; grimNext = 4 + random.nextInt(5); }

        double px = player.getX(), py = player.getY(), pz = player.getZ();
        float yaw = player.getYaw(), pitch = player.getPitch();
        double yOff = random.nextDouble() * 0.03 - 0.015;

        player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(
            px + mx, py + vy + yOff, pz + mz, yaw, pitch, spoof));
        player.setVelocity(mx, vy, mz);
        player.fallDistance = 0;
    }

    private void tickMatrix(MinecraftClient mc) {
        var player = mc.player;
        var opts = mc.options;
        player.getAbilities().allowFlying = false;
        player.getAbilities().flying = false;

        Vec3d vel = moveVec(player.getYaw(), opts);
        double mx = vel.x, mz = vel.z;
        double h = Math.sqrt(mx * mx + mz * mz);
        double cap = Math.min(speed, 0.22);
        if (h > cap) { mx = mx / h * cap; mz = mz / h * cap; }

        tick++;
        double vy;
        if (opts.jumpKey.isPressed()) vy = 0.42;
        else if (opts.sneakKey.isPressed()) vy = -0.1;
        else vy = Math.max(player.getVelocity().y - 0.08, -0.5);

        player.setVelocity(mx, vy, mz);
        player.fallDistance = 0;
    }

    private void tickVulcan(MinecraftClient mc) {
        var player = mc.player;
        var opts = mc.options;
        player.getAbilities().allowFlying = false;
        player.getAbilities().flying = false;

        Vec3d vel = moveVec(player.getYaw(), opts);
        double mx = vel.x, mz = vel.z;
        double h = Math.sqrt(mx * mx + mz * mz);
        double cap = Math.min(speed, 0.18);
        if (h > cap) { mx = mx / h * cap; mz = mz / h * cap; }
        double vy = vert(opts);

        tick++;
        double px = player.getX(), py = player.getY(), pz = player.getZ();
        float yaw = player.getYaw(), pitch = player.getPitch();
        boolean onGround = (tick + phaseOffset) % 2 == 0;
        double yOff = random.nextDouble() * 0.04 - 0.02;

        player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(
            px + mx, py + vy + yOff, pz + mz, yaw, pitch, onGround));
        player.setVelocity(mx, vy, mz);
        player.fallDistance = 0;
    }

    private int intaveTeleTimer = 0;

    private void tickIntave(MinecraftClient mc) {
        var player = mc.player;
        var opts = mc.options;
        player.getAbilities().allowFlying = false;
        player.getAbilities().flying = false;

        Vec3d vel = moveVec(player.getYaw(), opts);
        double mx = vel.x, mz = vel.z;
        double h = Math.sqrt(mx * mx + mz * mz);
        double cap = Math.min(speed, 0.26);
        if (h > cap) { mx = mx / h * cap; mz = mz / h * cap; }
        double vy = vert(opts);

        if (opts.jumpKey.isPressed()) {
            intaveTeleTimer++;
            if (intaveTeleTimer >= 20) {
                intaveTeleTimer = 0;
                double px = player.getX(), py = player.getY(), pz = player.getZ();
                player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(
                    px, py + 0.5, pz, player.getYaw(), player.getPitch(), false));
                player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(
                    px, py + 0.5, pz, player.getYaw(), player.getPitch(), true));
            }
        } else intaveTeleTimer = 0;

        double px = player.getX(), py = player.getY(), pz = player.getZ();
        player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(
            px + mx, py + vy, pz + mz, player.getYaw(), player.getPitch(), true));
        player.setVelocity(mx, vy, mz);
        player.fallDistance = 0;
    }

    private Vec3d moveVec(float yawDeg, net.minecraft.client.option.GameOptions opts) {
        double yaw = Math.toRadians(yawDeg);
        double vx = 0, vz = 0;
        if (opts.forwardKey.isPressed()) { vx -= Math.sin(yaw) * speed; vz += Math.cos(yaw) * speed; }
        if (opts.backKey.isPressed())    { vx += Math.sin(yaw) * speed; vz -= Math.cos(yaw) * speed; }
        if (opts.leftKey.isPressed())    { vx -= Math.cos(yaw) * speed; vz -= Math.sin(yaw) * speed; }
        if (opts.rightKey.isPressed())   { vx += Math.cos(yaw) * speed; vz += Math.sin(yaw) * speed; }
        double len = Math.sqrt(vx * vx + vz * vz);
        if (len > 1) { vx /= len; vz /= len; }
        return new Vec3d(vx, 0, vz);
    }

    private double vert(net.minecraft.client.option.GameOptions opts) {
        if (opts.jumpKey.isPressed()) return speedUp;
        if (opts.sneakKey.isPressed()) return -speedDown;
        return 0;
    }
}
