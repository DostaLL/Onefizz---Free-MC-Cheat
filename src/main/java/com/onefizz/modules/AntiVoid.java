package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

import java.util.LinkedList;

public class AntiVoid extends Module {

    @Setting(name = "Trigger Y", min = -64f, max = 0f)
    public float triggerY = -10f;

    private final LinkedList<Vec3d> safePositions = new LinkedList<>();

    public AntiVoid() { super("AntiVoid", "Защита от падения в бездну"); }

    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null) return;

        if (mc.player.isOnGround()) {
            safePositions.addLast(mc.player.getPos());
            while (safePositions.size() > 5) safePositions.removeFirst();
            return;
        }

        if (!safePositions.isEmpty() && mc.player.getY() < triggerY) {
            Vec3d safePos = safePositions.getFirst();
            mc.player.setPosition(safePos.x, safePos.y, safePos.z);
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                safePos.x, safePos.y, safePos.z, true));
            mc.player.fallDistance = 0;
            mc.player.setVelocity(0, 0, 0);
        }
    }
}
