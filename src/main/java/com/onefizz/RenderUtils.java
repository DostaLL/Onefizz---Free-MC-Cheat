package com.onefizz;

import net.minecraft.client.gui.DrawContext;

public final class RenderUtils {

    public static void roundedRect(DrawContext ctx, int x1, int y1, int x2, int y2, int radius, int color) {
        if (radius <= 0) { ctx.fill(x1, y1, x2, y2, color); return; }
        if (x2 - x1 < radius * 2 || y2 - y1 < radius * 2) { ctx.fill(x1, y1, x2, y2, color); return; }
        ctx.fill(x1, y1 + radius, x2, y2 - radius, color);
        ctx.fill(x1 + radius, y1, x2 - radius, y1 + radius, color);
        ctx.fill(x1 + radius, y2 - radius, x2 - radius, y2, color);
        for (int dy = 0; dy < radius; dy++) {
            int yOff = radius - dy - 1;
            double exact = radius - Math.sqrt((double) radius * radius - (double) yOff * yOff);
            int dx = (int) Math.ceil(exact);
            int leftStart = x1 + dx;
            int rightEnd = x2 - dx;
            ctx.fill(leftStart, y1 + dy, x1 + radius, y1 + dy + 1, color);
            ctx.fill(x2 - radius, y1 + dy, rightEnd, y1 + dy + 1, color);
            ctx.fill(leftStart, y2 - dy - 1, x1 + radius, y2 - dy, color);
            ctx.fill(x2 - radius, y2 - dy - 1, rightEnd, y2 - dy, color);
            float frac = (float) (exact - Math.floor(exact));
            if (frac > 0.1f) {
                int aa = withAlpha(color, frac * 0.6f);
                int aaX = x1 + dx - 1;
                int aaXR = x2 - dx;
                if (aaX >= x1) {
                    ctx.fill(aaX, y1 + dy, aaX + 1, y1 + dy + 1, aa);
                    ctx.fill(aaX, y2 - dy - 1, aaX + 1, y2 - dy, aa);
                }
                if (aaXR < x2) {
                    ctx.fill(aaXR, y1 + dy, aaXR + 1, y1 + dy + 1, aa);
                    ctx.fill(aaXR, y2 - dy - 1, aaXR + 1, y2 - dy, aa);
                }
            }
        }
    }

    public static void roundedOutline(DrawContext ctx, int x1, int y1, int x2, int y2, int radius, int color) {
        ctx.fill(x1 + radius, y1, x2 - radius, y1 + 1, color);
        ctx.fill(x1 + radius, y2 - 1, x2 - radius, y2, color);
        ctx.fill(x1, y1 + radius, x1 + 1, y2 - radius, color);
        ctx.fill(x2 - 1, y1 + radius, x2, y2 - radius, color);
        for (int dy = 0; dy < radius; dy++) {
            int yOff = radius - dy - 1;
            double exact = radius - Math.sqrt((double) radius * radius - (double) yOff * yOff);
            int dx = (int) Math.ceil(exact);
            ctx.fill(x1 + dx, y1 + dy, x1 + dx + 1, y1 + dy + 1, color);
            ctx.fill(x2 - dx - 1, y1 + dy, x2 - dx, y1 + dy + 1, color);
            ctx.fill(x1 + dx, y2 - dy - 1, x1 + dx + 1, y2 - dy, color);
            ctx.fill(x2 - dx - 1, y2 - dy - 1, x2 - dx, y2 - dy, color);
            float frac = (float) (exact - Math.floor(exact));
            if (frac > 0.15f) {
                int aa = withAlpha(color, frac * 0.5f);
                int aaX = x1 + dx - 1;
                int aaXR = x2 - dx;
                if (aaX >= x1) {
                    ctx.fill(aaX, y1 + dy, aaX + 1, y1 + dy + 1, aa);
                    ctx.fill(aaX, y2 - dy - 1, aaX + 1, y2 - dy, aa);
                }
                if (aaXR < x2) {
                    ctx.fill(aaXR, y1 + dy, aaXR + 1, y1 + dy + 1, aa);
                    ctx.fill(aaXR, y2 - dy - 1, aaXR + 1, y2 - dy, aa);
                }
            }
        }
    }

    public static void shadowRect(DrawContext ctx, int x1, int y1, int x2, int y2, int intensity) {
        for (int i = intensity; i > 0; i--) {
            float alpha = 0.12f * i / intensity;
            int c = (int)(alpha * 255) << 24;
            ctx.fill(x1 - i, y1 - i, x2 + i, y1 - i + 1, c);
            ctx.fill(x1 - i, y2 + i - 1, x2 + i, y2 + i, c);
            ctx.fill(x1 - i, y1 - i, x1 - i + 1, y2 + i, c);
            ctx.fill(x2 + i - 1, y1 - i, x2 + i, y2 + i, c);
        }
    }

    public static void gradientRect(DrawContext ctx, int x1, int y1, int x2, int y2, int color1, int color2, boolean vertical) {
        if (vertical) { vGradient(ctx, x1, y1, x2, y2, color1, color2); }
        else { hGradient(ctx, x1, y1, x2, y2, color1, color2); }
    }

    public static void vGradient(DrawContext ctx, int x1, int y1, int x2, int y2, int topColor, int bottomColor) {
        int steps = Math.max(1, y2 - y1);
        for (int i = 0; i < steps; i++) {
            float t = (float) i / steps;
            int c = lerpColor(topColor, bottomColor, t);
            ctx.fill(x1, y1 + i, x2, y1 + i + 1, c);
        }
    }

    public static void hGradient(DrawContext ctx, int x1, int y1, int x2, int y2, int leftColor, int rightColor) {
        int steps = Math.max(1, x2 - x1);
        for (int i = 0; i < steps; i++) {
            float t = (float) i / steps;
            int c = lerpColor(leftColor, rightColor, t);
            ctx.fill(x1 + i, y1, x1 + i + 1, y2, c);
        }
    }

    public static void circle(DrawContext ctx, int cx, int cy, int radius, int color) {
        for (int dy = -radius; dy <= radius; dy++) {
            int dx = (int) Math.sqrt((double) radius * radius - (double) dy * dy);
            ctx.fill(cx - dx, cy + dy, cx + dx, cy + dy + 1, color);
        }
    }

    public static void oval(DrawContext ctx, int x1, int y1, int x2, int y2, int color) {
        float rx = (x2 - x1) / 2f;
        float ry = (y2 - y1) / 2f;
        float cxf = x1 + rx;
        float cyf = y1 + ry;
        int h = y2 - y1;
        for (int dy = 0; dy < h; dy++) {
            float yNorm = (dy - ry) / ry;
            float xSpan = rx * (float) Math.sqrt(Math.max(0, 1.0 - yNorm * yNorm));
            int lx = (int) Math.ceil(cxf - xSpan);
            int rxEnd = (int) Math.floor(cxf + xSpan);
            ctx.fill(lx, y1 + dy, rxEnd, y1 + dy + 1, color);
        }
    }

    public static int lerpColor(int a, int b, float t) {
        int aa = (a >>> 24) & 0xFF, ar = (a >>> 16) & 0xFF, ag = (a >>> 8) & 0xFF, ab = a & 0xFF;
        int ba = (b >>> 24) & 0xFF, br = (b >>> 16) & 0xFF, bg = (b >>> 8) & 0xFF, bb = b & 0xFF;
        int ra = (int)(aa + (ba - aa) * t);
        int rr = (int)(ar + (br - ar) * t);
        int rg = (int)(ag + (bg - ag) * t);
        int rb = (int)(ab + (bb - ab) * t);
        return (ra << 24) | (rr << 16) | (rg << 8) | rb;
    }

    public static int withAlpha(int color, float alphaFactor) {
        int a = (color >>> 24) & 0xFF;
        int newA = Math.max(0, Math.min(255, (int)(a * alphaFactor)));
        return (newA << 24) | (color & 0x00FFFFFF);
    }

    public static float easeOutCubic(float t) {
        float p = 1f - t;
        return 1f - p * p * p;
    }

    public static float easeInOutCubic(float t) {
        return t < 0.5f ? 4f * t * t * t : 1f - (float) Math.pow(-2f * t + 2f, 3) / 2f;
    }
}
