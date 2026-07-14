package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

/**
 * AutoArmor — каждый тик ищет лучшую броню в инвентаре и одевает её.
 * Использует swap-операции через ClickSlotC2SPacket.
 */
public class AutoArmor extends Module {

    @Setting public boolean head = true;
    @Setting public boolean chest = true;
    @Setting public boolean legs = true;
    @Setting public boolean feet = true;

    @Setting(min = 1f, max = 20f)
    public int delay = 5; // ticks between swaps

    private int tickCounter = 0;

    public AutoArmor() { super("AutoArmor", "Автоматическая смена брони"); }

    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null) return;
        if (mc.currentScreen != null) return; // не лезем в открытый инвентарь

        tickCounter++;
        if (tickCounter < delay) return;
        tickCounter = 0;

        if (head)  trySwap(mc, EquipmentSlot.HEAD,  5);
        if (chest) trySwap(mc, EquipmentSlot.CHEST, 6);
        if (legs)  trySwap(mc, EquipmentSlot.LEGS,  7);
        if (feet)  trySwap(mc, EquipmentSlot.FEET,  8);
    }

    private void trySwap(MinecraftClient mc, EquipmentSlot slot, int armorScreenSlot) {
        var player = mc.player;
        var inv = player.getInventory();
        ItemStack equipped = player.getEquippedStack(slot);

        float equippedScore = scoreArmor(equipped, slot);

        int bestSlot = -1;
        float bestScore = equippedScore;

        for (int i = 0; i < 36; i++) {
            ItemStack stack = inv.getStack(i);
            if (!(stack.getItem() instanceof ArmorItem armor)) continue;
            if (armor.getSlotType() != slot) continue;
            float score = scoreArmor(stack, slot);
            if (score > bestScore) {
                bestScore = score;
                bestSlot = i;
            }
        }
        // No better armor found in inventory (or equipped is already best)
        if (bestSlot == -1) return;

        // Convert hotbar/main slot to screen slot in player inventory screen
        // Player inventory screen: armor=5-8, main=9-35, hotbar=36-44
        int screenSlot = bestSlot < 9 ? 36 + bestSlot : bestSlot;

        var handler = player.currentScreenHandler;
        Int2ObjectMap<ItemStack> changed = new Int2ObjectOpenHashMap<>();
        changed.put(armorScreenSlot, inv.getStack(bestSlot));
        changed.put(screenSlot, equipped);
        player.networkHandler.sendPacket(new ClickSlotC2SPacket(
            handler.syncId,
            handler.nextRevision(),
            armorScreenSlot,
            0,
            SlotActionType.SWAP,
            ItemStack.EMPTY,
            changed
        ));

        // Local update
        ItemStack newPiece = inv.getStack(bestSlot).copy();
        inv.setStack(bestSlot, equipped);
        player.equipStack(slot, newPiece);
    }

    private float scoreArmor(ItemStack stack, EquipmentSlot slot) {
        if (!(stack.getItem() instanceof ArmorItem armor)) return -1f;
        if (armor.getSlotType() != slot) return -1f;
        // Use protection + toughness as primary metric
        return armor.getProtection() + armor.getToughness() * 0.5f;
    }
}
