package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

import java.util.Set;

public class ChestStealer extends Module {

    @Setting(name = "Задержка", min = 1f, max = 10f)
    public int delay = 2;

    @Setting(name = "Только ценное")
    public boolean onlyValuable = false;

    private static final Set<Item> VALUABLE = Set.of(
        Items.DIAMOND, Items.DIAMOND_BLOCK, Items.DIAMOND_SWORD, Items.DIAMOND_PICKAXE,
        Items.DIAMOND_AXE, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_HELMET,
        Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS,
        Items.EMERALD, Items.EMERALD_BLOCK,
        Items.NETHERITE_INGOT, Items.NETHERITE_BLOCK, Items.NETHERITE_SWORD,
        Items.NETHERITE_PICKAXE, Items.NETHERITE_AXE, Items.NETHERITE_CHESTPLATE,
        Items.NETHERITE_HELMET, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS,
        Items.ENCHANTED_BOOK
    );

    private int tickCounter = 0;

    public ChestStealer() { super("ChestStealer", "Автоматический лут сундуков"); }

    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null || mc.interactionManager == null) return;
        if (!(mc.currentScreen instanceof GenericContainerScreen screen)) return;
        if (++tickCounter < delay) return;
        tickCounter = 0;
        var handler = screen.getScreenHandler();
        int chestSlots = handler.getRows() * 9;
        for (int i = 0; i < chestSlots; i++) {
            ItemStack stack = handler.getSlot(i).getStack();
            if (stack.isEmpty()) continue;
            if (onlyValuable && !isValuable(stack)) continue;
            mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
            return;
        }
    }

    private boolean isValuable(ItemStack stack) {
        if (VALUABLE.contains(stack.getItem())) return true;
        return stack.get(DataComponentTypes.STORED_ENCHANTMENTS) != null;
    }
}
