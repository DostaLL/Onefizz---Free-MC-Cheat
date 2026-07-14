package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

import java.util.Random;

public class Criticals extends Module {

    public enum Mode { PACKET, JUMP }

    @Setting
    public Mode mode = Mode.PACKET;

    private final Random random = new Random();
    private int packetPattern = 0;

    public Criticals() { super("Criticals", "Критические удары в прыжке"); }

    public void prepareHit(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null) return;
        if (!canCrit(mc)) return;

        switch (mode) {
            case JUMP -> {
                // JUMP mode requires being on ground to initiate the jump
                if (!mc.player.isOnGround()) return;
                mc.player.setVelocity(
                        mc.player.getVelocity().x,
                        0.11,
                        mc.player.getVelocity().z);
            }
            case PACKET -> {
                // PACKET mode works regardless of actual ground state
                double px = mc.player.getX();
                double py = mc.player.getY();
                double pz = mc.player.getZ();
                float yaw = mc.player.getYaw();
                float pitch = mc.player.getPitch();

                // Simulate falling: up then down with onGround=false
                // Server sees player airborne → next hit is critical
                double off = 0.0625;
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(
                    px, py + off, pz, yaw, pitch, false));
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(
                    px, py, pz, yaw, pitch, false));
            }
        }
    }

    public boolean shouldFakeAirPacket() {
        return false; // handled in prepareHit directly now
    }

    private boolean canCrit(MinecraftClient mc) {
        var p = mc.player;
        return !p.isTouchingWater()
                && !p.isInLava()
                && !p.hasStatusEffect(StatusEffects.BLINDNESS)
                && !p.hasStatusEffect(StatusEffects.SLOW_FALLING)
                && p.getVehicle() == null
                && !p.isClimbing();
    }
}
