package com.onefizz.modules;

import com.onefizz.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.screen.slot.SlotActionType;

/**
 * AutoTotem — автоматически перемещает тотем в оффхенд.
 *
 * Принцип:
 * 1. Каждый тик проверяем оффхенд.
 * 2. Если в оффхенде нет тотема — ищем тотем в инвентаре.
 * 3. Перемещаем через пакет ClickSlotC2SPacket (swap с оффхендом, клавиша F = slot 40).
 * 4. Работает даже при открытом инвентаре.
 */
public class AutoTotem extends Module {

    public AutoTotem() {
        super("AutoTotem", "Автоматический тотем в руке");
    }

    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null) return;
        var player = mc.player;

        // Оффхенд уже с тотемом — ничего не делаем
        if (player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) return;

        // Ищем тотем в инвентаре (слоты 0-35 + хотбар 0-8)
        var inv = player.getInventory();
        int totemSlot = -1;
        for (int i = 0; i < 36; i++) {
            if (inv.getStack(i).getItem() == Items.TOTEM_OF_UNDYING) {
                totemSlot = i;
                break;
            }
        }
        if (totemSlot == -1) return;

        var handler = player.currentScreenHandler;
        int syncId = handler.syncId;

        // Convert inventory slot to screen slot
        // Player inventory: hotbar = 36-44, main = 9-35, offhand = 45
        // With open container, slots shift by container size
        int containerSlots = handler.slots.size() - 36; // 36 = 27 main + 9 hotbar
        int invOffset = containerSlots > 0 ? containerSlots : 0;
        int screenSlot = totemSlot < 9
            ? invOffset + 36 + totemSlot   // hotbar
            : invOffset + totemSlot;       // main inventory
        int offhandSlot = invOffset + 45;

        Int2ObjectMap<ItemStack> changed = new Int2ObjectOpenHashMap<>();
        changed.put(screenSlot, player.getOffHandStack());
        changed.put(offhandSlot, inv.getStack(totemSlot));
        mc.player.networkHandler.sendPacket(new ClickSlotC2SPacket(
            syncId,
            handler.nextRevision(),
            screenSlot,
            40,
            SlotActionType.SWAP,
            ItemStack.EMPTY,
            changed
        ));

        // Обновляем локально
        ItemStack totem = inv.getStack(totemSlot);
        ItemStack offhand = player.getOffHandStack().copy();
        player.getInventory().setStack(totemSlot, offhand);
        player.getInventory().offHand.set(0, totem);
    }
}
