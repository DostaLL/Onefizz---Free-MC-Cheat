package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class AutoGapple extends Module {

    @Setting(min = 1f, max = 20f)
    public float threshold = 14.0f;

    @Setting
    public boolean preferEnchanted = false;

    private int prevSlot = -1;
    private boolean eating = false;

    public AutoGapple() {
        super("AutoGapple", "Автоматическое золотое яблоко");
    }

    @Override
    protected void onDisable() {
        stopEating(MinecraftClient.getInstance());
    }

    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null) return;
        var player = mc.player;

        if (eating) {
            if (!player.isUsingItem()) {
                eating = false;
                restoreSlot(mc);
            } else {
                mc.interactionManager.interactItem(player, Hand.MAIN_HAND);
            }
            return;
        }

        if (player.getHealth() >= threshold) return;

        int gappleSlot = findGapple(mc);
        if (gappleSlot == -1) return;

        prevSlot = player.getInventory().selectedSlot;
        player.getInventory().selectedSlot = gappleSlot;

        eating = true;
        mc.interactionManager.interactItem(player, Hand.MAIN_HAND);
    }

    private int findGapple(MinecraftClient mc) {
        var inv = mc.player.getInventory();
        int enchSlot = -1, normalSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE) enchSlot = i;
            else if (stack.getItem() == Items.GOLDEN_APPLE) normalSlot = i;
        }
        if (preferEnchanted && enchSlot != -1) return enchSlot;
        if (normalSlot != -1) return normalSlot;
        return enchSlot;
    }

    private void restoreSlot(MinecraftClient mc) {
        if (prevSlot != -1 && mc.player != null) {
            mc.player.getInventory().selectedSlot = prevSlot;
            prevSlot = -1;
        }
    }

    private void stopEating(MinecraftClient mc) {
        if (eating && mc.player != null) {
            eating = false;
            restoreSlot(mc);
        }
    }
}
