package com.onefizz;

import net.minecraft.client.gui.DrawContext;

public final class SakuraTree {

    private static final int TRUNK     = 0xCC2A1B12;
    private static final int TRUNK_DK  = 0xCC1A0F08;
    private static final int LEAF_DK   = 0xCC9A2D5C;
    private static final int LEAF_MD   = 0xCCD9527E;
    private static final int LEAF_LT   = 0xCCFF8FB1;
    private static final int LEAF_HL   = 0xCCFFB7CE;

    public static void drawCornerTree(DrawContext ctx, int baseX, int baseY, float scale, float alpha) {
        int s = (int) Math.max(1, scale);

        drawTrunkSegment(ctx, baseX - 4 * s, baseY - 18 * s, 6 * s, 18 * s, TRUNK, alpha);
        drawTrunkSegment(ctx, baseX - 8 * s, baseY - 28 * s, 5 * s, 12 * s, TRUNK, alpha);
        drawTrunkSegment(ctx, baseX - 13 * s, baseY - 36 * s, 4 * s, 10 * s, TRUNK, alpha);
        drawTrunkSegment(ctx, baseX - 4 * s, baseY - 18 * s, 1 * s, 18 * s, TRUNK_DK, alpha);
        drawTrunkSegment(ctx, baseX - 8 * s, baseY - 28 * s, 1 * s, 12 * s, TRUNK_DK, alpha);

        drawTrunkSegment(ctx, baseX - 17 * s, baseY - 42 * s, 3 * s, 8 * s, TRUNK, alpha);
        drawTrunkSegment(ctx, baseX - 22 * s, baseY - 32 * s, 8 * s, 2 * s, TRUNK, alpha);
        drawTrunkSegment(ctx, baseX - 28 * s, baseY - 36 * s, 6 * s, 2 * s, TRUNK, alpha);
        drawTrunkSegment(ctx, baseX - 6 * s, baseY - 30 * s, 8 * s, 2 * s, TRUNK, alpha);
        drawTrunkSegment(ctx, baseX, baseY - 34 * s, 6 * s, 2 * s, TRUNK, alpha);

        drawLeafCloud(ctx, baseX - 18 * s, baseY - 48 * s, 11 * s, LEAF_DK, alpha);
        drawLeafCloud(ctx, baseX - 26 * s, baseY - 42 * s, 9 * s, LEAF_DK, alpha);
        drawLeafCloud(ctx, baseX - 8 * s, baseY - 42 * s, 9 * s, LEAF_DK, alpha);
        drawLeafCloud(ctx, baseX - 16 * s, baseY - 50 * s, 9 * s, LEAF_MD, alpha);
        drawLeafCloud(ctx, baseX - 22 * s, baseY - 44 * s, 7 * s, LEAF_MD, alpha);
        drawLeafCloud(ctx, baseX - 10 * s, baseY - 44 * s, 7 * s, LEAF_MD, alpha);
        drawLeafCloud(ctx, baseX - 17 * s, baseY - 52 * s, 6 * s, LEAF_LT, alpha);
        drawLeafCloud(ctx, baseX - 23 * s, baseY - 46 * s, 5 * s, LEAF_LT, alpha);
        drawLeafCloud(ctx, baseX - 11 * s, baseY - 46 * s, 5 * s, LEAF_LT, alpha);
        drawLeafCloud(ctx, baseX - 17 * s, baseY - 53 * s, 3 * s, LEAF_HL, alpha);
        drawLeafCloud(ctx, baseX - 23 * s, baseY - 47 * s, 2 * s, LEAF_HL, alpha);
        drawLeafCloud(ctx, baseX - 11 * s, baseY - 47 * s, 2 * s, LEAF_HL, alpha);
    }

    private static void drawLeafCloud(DrawContext ctx, int cx, int cy, int radius, int color, float alpha) {
        if (radius <= 0) return;
        int finalA = (int) (((color >>> 24) & 0xFF) * alpha);
        if (finalA < 4) return;
        int finalColor = (finalA << 24) | (color & 0x00FFFFFF);
        for (int dy = -radius; dy <= radius; dy++) {
            int dx = (int) Math.sqrt(radius * radius - dy * dy);
            ctx.fill(cx - dx, cy + dy, cx + dx + 1, cy + dy + 1, finalColor);
        }
    }

    private static void drawTrunkSegment(DrawContext ctx, int x, int y, int w, int h, int color, float alpha) {
        if (w <= 0 || h <= 0) return;
        int finalA = (int) (((color >>> 24) & 0xFF) * alpha);
        if (finalA < 4) return;
        int finalColor = (finalA << 24) | (color & 0x00FFFFFF);
        ctx.fill(x, y, x + w, y + h, finalColor);
    }
}
