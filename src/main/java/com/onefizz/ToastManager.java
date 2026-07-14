package com.onefizz;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class ToastManager {

    public static final ToastManager INSTANCE = new ToastManager();

    private static final int TOAST_W    = 140;
    private static final int TOAST_H    = 22;
    private static final int MARGIN     = 6;
    private static final long SHOW_MS   = 2000;
    private static final long ANIM_MS   = 200;

    private final List<Toast> active = new ArrayList<>();
    private final Deque<Toast> queue  = new ArrayDeque<>();

    public void push(String moduleName, boolean enabled) {
        queue.add(new Toast(moduleName, enabled, System.currentTimeMillis()));
    }

    public void render(DrawContext ctx, int screenW) {
        long now = System.currentTimeMillis();

        // Drain queue into active
        while (!queue.isEmpty()) active.add(queue.poll());

        // Remove expired
        active.removeIf(t -> now - t.createdAt > SHOW_MS + ANIM_MS);

        int y = MARGIN;
        for (Toast t : active) {
            long age = now - t.createdAt;
            float alpha;
            if (age < ANIM_MS) {
                alpha = (float) age / ANIM_MS;
            } else if (age > SHOW_MS) {
                alpha = 1f - (float)(age - SHOW_MS) / ANIM_MS;
            } else {
                alpha = 1f;
            }
            alpha = Math.max(0f, Math.min(1f, alpha));

            int slideX = (int)((1f - alpha) * (TOAST_W + MARGIN));
            int tx = screenW - TOAST_W - MARGIN + slideX;

            int bgAlpha  = (int)(alpha * 0xE0);
            int dotColor = t.enabled ? 0xFF9D4EDD : 0xFF3D3550;

            // Background
            ctx.fill(tx, y, tx + TOAST_W, y + TOAST_H,
                (bgAlpha << 24) | 0x060410);
            // Left accent bar
            ctx.fill(tx, y, tx + 2, y + TOAST_H,
                (int)(alpha * 0xFF) << 24 | 0x7C3AED);
            // Dot
            ctx.fill(tx + TOAST_W - 10, y + 8, tx + TOAST_W - 4, y + 14,
                (int)(alpha * 0xFF) << 24 | (dotColor & 0x00FFFFFF));

            MinecraftClient mc = MinecraftClient.getInstance();
            String label = t.moduleName;
            String status = t.enabled ? "ON" : "OFF";
            int textAlpha = (int)(alpha * 0xFF);
            int textColor = (textAlpha << 24) | 0xE2D9F3;
            int statusColor = t.enabled
                ? (textAlpha << 24) | 0x9D4EDD
                : (textAlpha << 24) | 0x4A4060;

            ctx.drawText(mc.textRenderer, label, tx + 6, y + 4, textColor, false);
            ctx.drawText(mc.textRenderer, status,
                tx + TOAST_W - 14 - mc.textRenderer.getWidth(status),
                y + 4, statusColor, false);

            y += TOAST_H + MARGIN;
        }
    }

    private static class Toast {
        final String moduleName;
        final boolean enabled;
        final long createdAt;
        Toast(String n, boolean e, long t) { moduleName = n; enabled = e; createdAt = t; }
    }
}
