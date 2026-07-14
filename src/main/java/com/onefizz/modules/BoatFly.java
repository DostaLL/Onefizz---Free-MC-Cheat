package com.onefizz.modules;

import com.onefizz.AntiLagback;
import com.onefizz.Module;
import com.onefizz.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;

import java.util.Random;

public class BoatFly extends Module {

    public enum Mode { NORMAL, GRIM_LEGACY, MATRIX, VULCAN }

    @Setting(name = "Режим")
    public Mode mode = Mode.GRIM_LEGACY;

    @Setting(name = "Скорость", min = 0.01f, max = 5f)
    public float speed = 0.28f;
    @Setting(name = "Вверх", min = 0.01f, max = 5f)
    public float speedUp = 0.1f;
    @Setting(name = "Вниз", min = 0.01f, max = 5f)
    public float speedDown = 0.1f;

    private int tick;
    private int packetDelay;
    private int packetNext;
    private int vulcanPhase;
    private BoatEntity ridingBoat;
    private final Random random = new Random();

    public BoatFly() { super("BoatFly", "Полет на лодке (Grim/Matrix)"); }

    @Override
    protected void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.player.getVehicle() instanceof BoatEntity) {
            mc.player.dismountVehicle();
        }
        ridingBoat = null;
        tick = 0;
        packetDelay = 0;
        packetNext = 4 + random.nextInt(4);
        vulcanPhase = 0;
    }

    @Override
    protected void onEnable() {
        tick = 0;
        packetDelay = 0;
        packetNext = 4 + random.nextInt(4);
        vulcanPhase = 0;
    }

    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null || mc.world == null) return;
        if (AntiLagback.isSuppressed()) return;
        switch (mode) {
            case NORMAL      -> tickNormal(mc);
            case GRIM_LEGACY -> tickGrimLegacy(mc);
            case MATRIX      -> tickMatrix(mc);
            case VULCAN      -> tickVulcan(mc);
        }
    }

    private BoatEntity findBoat(MinecraftClient mc) {
        return mc.world.getEntitiesByClass(BoatEntity.class,
            mc.player.getBoundingBox().expand(4.0), b -> !b.hasPassengers()
                && mc.player.distanceTo(b) <= 4.0)
            .stream().min((a, b) -> Double.compare(mc.player.distanceTo(a), mc.player.distanceTo(b)))
            .orElse(null);
    }

    private double[] hor(double yawDeg, double cap, net.minecraft.client.option.GameOptions opts) {
        double yaw = Math.toRadians(yawDeg);
        double vx = 0, vz = 0;
        if (opts.forwardKey.isPressed()) { vx -= Math.sin(yaw) * cap; vz += Math.cos(yaw) * cap; }
        if (opts.backKey.isPressed())    { vx += Math.sin(yaw) * cap; vz -= Math.cos(yaw) * cap; }
        if (opts.leftKey.isPressed())    { vx -= Math.cos(yaw) * cap; vz -= Math.sin(yaw) * cap; }
        if (opts.rightKey.isPressed())   { vx += Math.cos(yaw) * cap; vz += Math.sin(yaw) * cap; }
        return new double[]{vx, vz};
    }

    private void tickNormal(MinecraftClient mc) {
        var player = mc.player;
        if (!(player.getVehicle() instanceof BoatEntity boat)) {
            ridingBoat = findBoat(mc);
            if (ridingBoat != null) mc.interactionManager.interactEntity(player, ridingBoat, Hand.MAIN_HAND);
            return;
        }
        ride(boat, mc, Math.min(speed, 0.35), 0.1, 0.005);
    }

    private void tickGrimLegacy(MinecraftClient mc) {
        var player = mc.player;
        if (!(player.getVehicle() instanceof BoatEntity boat)) {
            ridingBoat = findBoat(mc);
            if (ridingBoat != null) mc.interactionManager.interactEntity(player, ridingBoat, Hand.MAIN_HAND);
            return;
        }
        ride(boat, mc, Math.min(speed, 0.22), 0.08, 0.002);
    }

    private void tickMatrix(MinecraftClient mc) {
        var player = mc.player;
        if (!(player.getVehicle() instanceof BoatEntity boat)) {
            ridingBoat = findBoat(mc);
            if (ridingBoat != null) mc.interactionManager.interactEntity(player, ridingBoat, Hand.MAIN_HAND);
            return;
        }
        ride(boat, mc, Math.min(speed, 0.22), 0.08, 0.003);
    }

    private void ride(BoatEntity boat, MinecraftClient mc, double cap, double vCap, double wobbleMag) {
        var player = mc.player;
        var opts = mc.options;
        double[] h = hor(player.getYaw(), cap, opts);
        double vx = h[0], vz = h[1];
        double vy;
        if (opts.jumpKey.isPressed()) vy = vCap;
        else if (opts.sneakKey.isPressed()) vy = -vCap;
        else vy = 0;
        tick++;
        vy += Math.sin(tick * 0.25) * wobbleMag;
        boat.setVelocity(vx, vy, vz);
        packetDelay++;
        if (packetDelay >= packetNext) {
            packetDelay = 0;
            packetNext = 4 + random.nextInt(4);
            player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(
                boat.getX(), boat.getY() + wobbleMag, boat.getZ(),
                player.getYaw(), player.getPitch(), true));
        }
    }

    private void tickVulcan(MinecraftClient mc) {
        var player = mc.player;
        if (!(player.getVehicle() instanceof BoatEntity boat)) {
            ridingBoat = findBoat(mc);
            if (ridingBoat != null) mc.interactionManager.interactEntity(player, ridingBoat, Hand.MAIN_HAND);
            return;
        }
        var opts = mc.options;
        double cap = Math.min(speed, 0.14);
        double[] h = hor(player.getYaw(), cap, opts);
        double vx = h[0], vz = h[1];
        double vy;
        if (opts.jumpKey.isPressed()) vy = Math.min(speedUp, 0.06);
        else if (opts.sneakKey.isPressed()) vy = -Math.min(speedDown, 0.06);
        else vy = 0;
        tick++;
        vy += random.nextDouble() * 0.008 - 0.004;
        boat.setVelocity(vx, vy, vz);
        packetDelay++;
        if (packetDelay >= packetNext) {
            packetDelay = 0;
            packetNext = 4 + random.nextInt(4);
            boolean onGround = (tick + random.nextInt(2)) % 2 == 0;
            player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(
                boat.getX(), boat.getY() - 0.001, boat.getZ(),
                player.getYaw(), player.getPitch(), onGround));
            player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(
                boat.getX(), boat.getY(), boat.getZ(),
                player.getYaw(), player.getPitch(), !onGround));
        }
    }
}
