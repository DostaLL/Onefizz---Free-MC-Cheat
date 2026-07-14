package com.onefizz.modules;

import com.onefizz.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class FastBreak extends Module {

    private int tick = 0;

    public FastBreak() { super("FastBreak", "Ускоренное разрушение блоков"); }

    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null || mc.interactionManager == null) return;
        if (!mc.interactionManager.isBreakingBlock()) { tick = 0; return; }
        if (!(mc.crosshairTarget instanceof BlockHitResult bhr) || mc.crosshairTarget.getType() == HitResult.Type.MISS) return;

        tick++;
        if (tick % 2 == 0) {
            BlockPos pos = bhr.getBlockPos();
            Direction side = bhr.getSide();
            mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, side));
        }
    }
}
