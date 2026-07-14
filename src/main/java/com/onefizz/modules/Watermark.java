package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.RenderUtils;
import com.onefizz.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/**
 * Watermark — логотип ONEFIZZ с информацией о сессии.
 */
public class Watermark extends Module {

    private long lastFpsUpdate = 0;
    private int currentFps = 0;

    public Watermark() { super("Watermark", "Логотип ONEFIZZ с FPS/TPS"); }

    public void onRender(DrawContext ctx, int screenW) {
        if (!isEnabled()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        long now = System.currentTimeMillis();
        if (now - lastFpsUpdate > 500) {
            currentFps = mc.getCurrentFps();
            lastFpsUpdate = now;
        }

        int x = 4;
        int y = 4;

        String main = "ONEFIZZ";
        String sub = String.format("  |  %dfps", currentFps);

        // Glow text effect (draw twice with offset)
        ctx.drawText(mc.textRenderer, Text.literal(main), x + 1, y + 1,
            RenderUtils.withAlpha(Theme.GLOW(), 0.4f), false);
        ctx.drawText(mc.textRenderer, Text.literal(main), x, y,
            0xFFFFFFFF, false);

        int mainW = mc.textRenderer.getWidth(main);
        ctx.drawText(mc.textRenderer, Text.literal(sub), x + mainW, y,
            0xFFA39BB7, false);
    }
}
