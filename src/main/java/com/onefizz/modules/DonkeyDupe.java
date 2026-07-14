package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

public class DonkeyDupe extends Module {

    @Setting(name = "Радиус поиска", min = 2f, max = 6f) public float searchRange = 4f;
    @Setting(name = "Задержка (тики)", min = 1f, max = 20f) public int delayTicks = 5;
    @Setting(name = "Авто-Оседлать") public boolean autoSaddle = true;
    @Setting(name = "Авто-Сундук") public boolean autoChest = true;
    @Setting(name = "Слотов для дюпа", min = 1f, max = 15f) public int dupeSlots = 5;

    private int timer = 0;
    private int stage = 0; // 0=idle, 1=open, 2=deposit, 3=close_reopen, 4=withdraw
    private AbstractDonkeyEntity target = null;
    private int syncId = -1;

    public DonkeyDupe() { super("DonkeyDupe", "Дюп предметов через осла"); }

    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null || mc.world == null || mc.interactionManager == null) return;

        if (timer > 0) { timer--; return; }

        var player = mc.player;

        // Если мы на лошади/осле — слезть для начала (если нужно)
        if (player.hasVehicle() && stage == 0) {
            player.networkHandler.sendPacket(new net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket(0, 0, false, true));
            timer = 5;
            return;
        }

        // Stage 0: найти цель
        if (stage == 0) {
            target = findTarget(mc);
            if (target == null) {
                player.sendMessage(net.minecraft.text.Text.literal("§c[DonkeyDupe] §fНет осла/ламы рядом"), true);
                toggle();
                return;
            }

            // Авто-оседлать
            if (autoSaddle && !target.isSaddled()) {
                int saddleSlot = findSlot(mc, Items.SADDLE);
                if (saddleSlot != -1) {
                    player.getInventory().selectedSlot = saddleSlot;
                    mc.interactionManager.interactEntity(player, target, Hand.MAIN_HAND);
                    timer = delayTicks;
                    return;
                }
            }

            // Авто-сундук
            if (autoChest && !target.hasChest()) {
                int chestSlot = findSlot(mc, Items.CHEST);
                if (chestSlot != -1) {
                    player.getInventory().selectedSlot = chestSlot;
                    mc.interactionManager.interactEntity(player, target, Hand.MAIN_HAND);
                    timer = delayTicks;
                    return;
                }
            }

            // Сесть на осла
            mc.interactionManager.interactEntity(player, target, Hand.MAIN_HAND);
            stage = 1;
            timer = delayTicks;
            return;
        }

        // Stage 1: открыть инвентарь (снова взаимодействуем — откроется GUI)
        if (stage == 1) {
            if (player.currentScreenHandler == null || player.currentScreenHandler.slots.size() < 10) {
                mc.interactionManager.interactEntity(player, target, Hand.MAIN_HAND);
                timer = delayTicks;
                return;
            }
            syncId = player.currentScreenHandler.syncId;
            stage = 2;
            timer = delayTicks;
            return;
        }

        // Stage 2: положить предметы в инвентарь осла
        if (stage == 2) {
            if (player.currentScreenHandler == null) { stage = 0; return; }

            int donkeySlots = player.currentScreenHandler.slots.size() - 36; // инвентарь игрока = 36 слотов
            int placed = 0;
            for (int i = 0; i < 36 && placed < dupeSlots; i++) {
                var stack = player.getInventory().getStack(i);
                if (stack.isEmpty()) continue;
                // Слоты осла начинаются после слотов игрока
                int donkeySlot = player.currentScreenHandler.slots.size() - 36 + placed;
                if (donkeySlot >= player.currentScreenHandler.slots.size()) break;

                mc.interactionManager.clickSlot(syncId, i, 0, SlotActionType.QUICK_MOVE, player);
                placed++;
            }

            player.sendMessage(net.minecraft.text.Text.literal("§a[DonkeyDupe] §fПоложено: " + placed), true);
            stage = 3;
            timer = 2; // минимальная задержка перед закрытием
            return;
        }

        // Stage 3: закрыть GUI → открыть снова (desync)
        if (stage == 3) {
            if (player.currentScreenHandler != null) {
                // Закрываем GUI пакетом
                player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(syncId));
                // Сразу пытаемся открыть снова — сервер еще не успел синхронизировать
                mc.interactionManager.interactEntity(player, target, Hand.MAIN_HAND);
            }
            stage = 4;
            timer = delayTicks;
            return;
        }

        // Stage 4: забираем предметы обратно (дюп произошел — предметы остались в entity)
        if (stage == 4) {
            if (player.currentScreenHandler == null) {
                stage = 0;
                toggle();
                player.sendMessage(net.minecraft.text.Text.literal("§a[DonkeyDupe] §fДюп завершен! Проверь инвентарь"), true);
                return;
            }

            // Забираем все из инвентаря осла обратно
            int donkeyStart = player.currentScreenHandler.slots.size() - 36;
            for (int i = donkeyStart; i < player.currentScreenHandler.slots.size() - 5; i++) {
                mc.interactionManager.clickSlot(syncId, i, 0, SlotActionType.QUICK_MOVE, player);
            }

            player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(syncId));
            stage = 0;
            timer = delayTicks;
            toggle();
            player.sendMessage(net.minecraft.text.Text.literal("§a[DonkeyDupe] §fДюп завершен!"), true);
        }
    }

    private AbstractDonkeyEntity findTarget(MinecraftClient mc) {
        AbstractDonkeyEntity best = null;
        double bestDist = searchRange * searchRange;
        for (Entity e : mc.world.getEntities()) {
            if (e instanceof AbstractDonkeyEntity donkey) {
                double dist = mc.player.squaredDistanceTo(donkey);
                if (dist < bestDist) {
                    bestDist = dist;
                    best = donkey;
                }
            }
        }
        return best;
    }

    private int findSlot(MinecraftClient mc, net.minecraft.item.Item item) {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == item) return i;
        }
        return -1;
    }

    @Override
    protected void onDisable() {
        stage = 0;
        timer = 0;
        target = null;
    }
}
