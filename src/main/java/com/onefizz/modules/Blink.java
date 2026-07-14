package com.onefizz.modules;

import com.onefizz.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class Blink extends Module {

    private double savedX, savedY, savedZ;
    private float savedYaw, savedPitch;
    private boolean hasSaved = false;

    public Blink() { super("Blink", "Заморозка позиции + телепорт"); }

    @Override
    protected void onEnable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            savedX = mc.player.getX();
            savedY = mc.player.getY();
            savedZ = mc.player.getZ();
            savedYaw = mc.player.getYaw();
            savedPitch = mc.player.getPitch();
            hasSaved = true;
        }
    }

    @Override
    protected void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && hasSaved) {
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(
                mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                mc.player.getYaw(), mc.player.getPitch(), mc.player.isOnGround()));
        }
        hasSaved = false;
    }

    public boolean isBlinking() { return isEnabled() && hasSaved; }
    public double getSavedX() { return savedX; }
    public double getSavedY() { return savedY; }
    public double getSavedZ() { return savedZ; }
    public float getSavedYaw() { return savedYaw; }
    public float getSavedPitch() { return savedPitch; }
}
