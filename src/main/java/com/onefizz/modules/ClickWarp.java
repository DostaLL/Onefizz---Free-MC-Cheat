package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class ClickWarp extends Module {

    @Setting(name = "Дальность", min = 10f, max = 50f) public float range = 20f;
    @Setting(name = "Плавный (тики без коллизии)") public int noClipTicks = 0;

    private int noClipTimer = 0;

    public ClickWarp() { super("ClickWarp", "Телепортация по клику мыши"); }

    public void onTick(MinecraftClient mc) {
        if (mc.player == null) return;

        if (noClipTimer > 0) {
            noClipTimer--;
            mc.player.noClip = true;
            if (noClipTimer == 0) {
                mc.player.noClip = false;
            }
        }
    }

    public boolean tryWarp(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null || mc.world == null) return false;

        HitResult hit = mc.crosshairTarget;
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) return false;

        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockPos pos = blockHit.getBlockPos();

        Vec3d eyePos = mc.player.getEyePos();
        Vec3d target = Vec3d.ofCenter(pos).add(0, 0.5, 0);

        if (eyePos.distanceTo(target) > range) return false;

        double x = target.x;
        double y = target.y;
        double z = target.z;

        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(
            x, y, z, mc.player.getYaw(), mc.player.getPitch(), true));

        mc.player.setPosition(x, y, z);

        if (noClipTicks > 0) {
            noClipTimer = noClipTicks;
            mc.player.noClip = true;
        }

        return true;
    }
}
