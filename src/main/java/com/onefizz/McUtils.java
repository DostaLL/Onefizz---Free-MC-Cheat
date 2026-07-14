package com.onefizz;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public final class McUtils {

    public static void sendRotationPacket(MinecraftClient mc, float yaw, float pitch) {
        if (mc.player == null) return;
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(
            mc.player.getX(), mc.player.getY(), mc.player.getZ(),
            yaw, pitch, mc.player.isOnGround()));
    }
}
