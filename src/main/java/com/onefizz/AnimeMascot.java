package com.onefizz;

import net.minecraft.client.gui.DrawContext;

public final class AnimeMascot {

    public enum Mood { NORMAL, HAPPY, HURT }

    private static final int SKIN     = 0xFFFFE0C4;
    private static final int SKIN_DK  = 0xFFE5C4A8;
    private static final int OUTLINE  = 0xFF2A1B3D;
    private static final int CHEEK    = 0xFFFF99B5;
    private static final int MOUTH    = 0xFFCC4466;
    private static final int WHITE    = 0xFFFFFFFF;
    private static final int EYE_BG   = 0xFFFFE0F0;

    public static void draw(DrawContext ctx, int x, int y, int size, Mood mood, boolean blink) {
        int hair  = Theme.A();
        int hairL = Theme.ALT();
        int hairD = darken(hair, 0.3f);
        int eye   = Theme.A();
        int cloth = Theme.A();

        rect(ctx, x, y, 2, 6, 4, 18, hair, size);
        rect(ctx, x, y, 1, 8, 1, 14, hairD, size);
        rect(ctx, x, y, 18, 6, 4, 18, hair, size);
        rect(ctx, x, y, 22, 8, 1, 14, hairD, size);
        rect(ctx, x, y, 4, 22, 16, 3, hair, size);
        rect(ctx, x, y, 3, 10, 1, 10, hairL, size);
        rect(ctx, x, y, 20, 10, 1, 10, hairL, size);

        for (int row = 0; row < 12; row++) {
            int rowY = 7 + row;
            int inset;
            if (row < 1) inset = 2;
            else if (row < 10) inset = 1;
            else inset = 2;
            int xS = 7 + inset, xE = 17 - inset;
            pixel(ctx, x, y, xS - 1, rowY, OUTLINE, size);
            pixel(ctx, x, y, xE, rowY, OUTLINE, size);
            for (int col = xS; col < xE; col++) pixel(ctx, x, y, col, rowY, SKIN, size);
        }
        for (int col = 9; col < 15; col++) pixel(ctx, x, y, col, 19, OUTLINE, size);

        for (int col = 6; col < 18; col++) {
            int distC = Math.abs(col - 12);
            int hairH;
            if (distC < 1) hairH = 2;
            else if (distC < 3) hairH = 4;
            else if (distC < 5) hairH = 5;
            else hairH = 4;
            for (int dy = 0; dy < hairH; dy++) pixel(ctx, x, y, col, 5 + dy, hair, size);
        }
        for (int col = 9; col < 15; col++) pixel(ctx, x, y, col, 6, hairL, size);

        rect(ctx, x, y, 5, 8, 2, 9, hair, size);
        rect(ctx, x, y, 17, 8, 2, 9, hair, size);
        pixel(ctx, x, y, 5, 10, hairL, size);
        pixel(ctx, x, y, 18, 10, hairL, size);

        pixel(ctx, x, y, 12, 2, hair, size);
        pixel(ctx, x, y, 12, 3, hair, size);
        pixel(ctx, x, y, 12, 4, hair, size);
        pixel(ctx, x, y, 13, 3, hairL, size);

        if (blink) {
            for (int col = 8; col < 11; col++) pixel(ctx, x, y, col, 12, OUTLINE, size);
            for (int col = 13; col < 16; col++) pixel(ctx, x, y, col, 12, OUTLINE, size);
        } else {
            drawEye(ctx, x, y, 8, 11, eye, size);
            drawEye(ctx, x, y, 13, 11, eye, size);
        }

        if (mood != Mood.HURT) {
            pixel(ctx, x, y, 7, 14, CHEEK, size);
            pixel(ctx, x, y, 8, 14, CHEEK, size);
            pixel(ctx, x, y, 15, 14, CHEEK, size);
            pixel(ctx, x, y, 16, 14, CHEEK, size);
        }

        switch (mood) {
            case HAPPY -> {
                pixel(ctx, x, y, 10, 16, MOUTH, size);
                pixel(ctx, x, y, 11, 17, MOUTH, size);
                pixel(ctx, x, y, 12, 17, MOUTH, size);
                pixel(ctx, x, y, 13, 16, MOUTH, size);
            }
            case HURT -> {
                pixel(ctx, x, y, 10, 17, MOUTH, size);
                pixel(ctx, x, y, 11, 16, MOUTH, size);
                pixel(ctx, x, y, 12, 16, MOUTH, size);
                pixel(ctx, x, y, 13, 17, MOUTH, size);
                pixel(ctx, x, y, 7, 15, 0xFF7DD3FC, size);
                pixel(ctx, x, y, 7, 16, 0xFF7DD3FC, size);
            }
            case NORMAL -> {
                pixel(ctx, x, y, 11, 17, MOUTH, size);
                pixel(ctx, x, y, 12, 17, MOUTH, size);
            }
        }

        rect(ctx, x, y, 11, 19, 2, 1, SKIN_DK, size);
        for (int row = 0; row < 5; row++) {
            int rowY = 20 + row;
            int inset = Math.max(0, 3 - row);
            int xS = 5 + inset, xE = 19 - inset;
            pixel(ctx, x, y, xS - 1, rowY, OUTLINE, size);
            pixel(ctx, x, y, xE, rowY, OUTLINE, size);
            for (int col = xS; col < xE; col++) {
                pixel(ctx, x, y, col, rowY, row == 0 ? hairL : cloth, size);
            }
        }
        pixel(ctx, x, y, 11, 20, WHITE, size);
        pixel(ctx, x, y, 12, 20, WHITE, size);
        pixel(ctx, x, y, 11, 21, hairL, size);
        pixel(ctx, x, y, 12, 21, hairL, size);
    }

    private static void drawEye(DrawContext ctx, int ox, int oy, int eX, int eY, int color, int size) {
        pixel(ctx, ox, oy, eX, eY, OUTLINE, size);
        pixel(ctx, ox, oy, eX + 1, eY, OUTLINE, size);
        pixel(ctx, ox, oy, eX + 2, eY, OUTLINE, size);
        for (int dy = 1; dy <= 2; dy++) {
            for (int dx = 0; dx <= 2; dx++) {
                pixel(ctx, ox, oy, eX + dx, eY + dy, color, size);
            }
        }
        pixel(ctx, ox, oy, eX + 1, eY + 2, OUTLINE, size);
        pixel(ctx, ox, oy, eX + 1, eY + 1, WHITE, size);
        pixel(ctx, ox, oy, eX + 2, eY + 2, EYE_BG, size);
        pixel(ctx, ox, oy, eX, eY + 3, OUTLINE, size);
        pixel(ctx, ox, oy, eX + 1, eY + 3, OUTLINE, size);
        pixel(ctx, ox, oy, eX + 2, eY + 3, OUTLINE, size);
    }

    private static int darken(int color, float factor) {
        int a = (color >>> 24) & 0xFF;
        int r = Math.max(0, (int) (((color >>> 16) & 0xFF) * (1 - factor)));
        int g = Math.max(0, (int) (((color >>> 8) & 0xFF) * (1 - factor)));
        int b = Math.max(0, (int) ((color & 0xFF) * (1 - factor)));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static void pixel(DrawContext ctx, int ox, int oy, int ax, int ay, int color, int size) {
        int sx = ox + ax * size;
        int sy = oy + ay * size;
        ctx.fill(sx, sy, sx + size, sy + size, color);
    }

    private static void rect(DrawContext ctx, int ox, int oy, int ax, int ay, int aw, int ah, int color, int size) {
        int sx = ox + ax * size;
        int sy = oy + ay * size;
        ctx.fill(sx, sy, sx + aw * size, sy + ah * size, color);
    }
}
