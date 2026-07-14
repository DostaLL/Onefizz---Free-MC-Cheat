package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.Setting;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public class AutoTool extends Module {

    @Setting(name = "Вернуть слот")
    public boolean restoreSlot = true;

    private int prevSlot = -1;
    private boolean wasBreaking = false;

    public AutoTool() { super("AutoTool", "Автосмена инструмента по блоку"); }

    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null || mc.interactionManager == null || mc.world == null) return;

        boolean breaking = mc.interactionManager.isBreakingBlock();
        if (!breaking) {
            if (wasBreaking && restoreSlot && prevSlot != -1) {
                mc.player.getInventory().selectedSlot = prevSlot;
                prevSlot = -1;
            }
            wasBreaking = false;
            return;
        }

        if (!(mc.crosshairTarget instanceof BlockHitResult bhr) || mc.crosshairTarget.getType() == HitResult.Type.MISS) return;
        BlockState state = mc.world.getBlockState(bhr.getBlockPos());
        int bestSlot = -1;
        float bestSpeed = 1f;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            float speed = stack.getMiningSpeedMultiplier(state);
            if (speed > bestSpeed) { bestSpeed = speed; bestSlot = i; }
        }
        if (bestSlot != -1) {
            if (!wasBreaking) prevSlot = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = bestSlot;
        }
        wasBreaking = true;
    }
}
