package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.ItemStack;

public class AutoEat extends Module {

    @Setting(name = "Порог голода", min = 1f, max = 19f)
    public int hungerThreshold = 17;

    private int prevSlot = -1;
    private boolean eating = false;

    public AutoEat() { super("AutoEat", "Автоматическое поедание еды"); }

    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null) return;
        int hunger = mc.player.getHungerManager().getFoodLevel();

        if (eating) {
            if (hunger >= 20 || !mc.player.isUsingItem()) {
                mc.options.useKey.setPressed(false);
                eating = false;
                if (prevSlot != -1) { mc.player.getInventory().selectedSlot = prevSlot; prevSlot = -1; }
            }
            return;
        }

        if (hunger >= hungerThreshold) return;

        int bestSlot = -1;
        int bestNutrition = 0;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            FoodComponent food = stack.get(DataComponentTypes.FOOD);
            if (food == null) continue;
            if (food.nutrition() > bestNutrition) {
                bestNutrition = food.nutrition();
                bestSlot = i;
            }
        }
        if (bestSlot == -1) return;

        prevSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = bestSlot;
        mc.options.useKey.setPressed(true);
        eating = true;
    }

    @Override
    protected void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (eating && mc != null) {
            mc.options.useKey.setPressed(false);
            if (prevSlot != -1 && mc.player != null) { mc.player.getInventory().selectedSlot = prevSlot; prevSlot = -1; }
        }
        eating = false;
    }
}
