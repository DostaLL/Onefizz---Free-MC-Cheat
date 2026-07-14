package com.onefizz;

import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class SakuraPetals {

    private static final int[] SAKURA_COLORS = {
        0xFFFF8FB1, 0xFFFFB7CE, 0xFFFF99B5, 0xFFFFD0DB, 0xFFFF7095, 0xFFE85A85,
    };

    public static class Petal {
        public float x, y;
        public float vx, vy;
        public float rotation;
        public float rotSpeed;
        public float size;
        public float swayPhase;
        public float swaySpeed;
        public int colorIdx;
        public float alpha;
    }

    private final List<Petal> petals = new ArrayList<>();
    private int width, height;
    private final Random random;

    public SakuraPetals() { this(0xCAFE); }

    public SakuraPetals(long seed) { this.random = new Random(seed); }

    public void init(int width, int height, int count) {
        this.width = width;
        this.height = height;
        petals.clear();
        for (int i = 0; i < count; i++) {
            Petal p = createPetal(true);
            petals.add(p);
        }
    }

    private Petal createPetal(boolean randomY) {
        Petal p = new Petal();
        p.x = random.nextFloat() * width;
        p.y = randomY ? random.nextFloat() * height : -10 - random.nextFloat() * 30;
        p.vx = (random.nextFloat() - 0.3f) * 8f;
        p.vy = 12f + random.nextFloat() * 18f;
        p.rotation = random.nextFloat() * 360f;
        p.rotSpeed = (random.nextFloat() - 0.5f) * 60f;
        p.size = 2.5f + random.nextFloat() * 2.5f;
        p.swayPhase = random.nextFloat() * (float) Math.PI * 2f;
        p.swaySpeed = 1.5f + random.nextFloat() * 1.5f;
        p.colorIdx = random.nextInt(SAKURA_COLORS.length);
        p.alpha = 0.5f + random.nextFloat() * 0.5f;
        return p;
    }

    public void tick(float dt) {
        for (Petal p : petals) {
            p.swayPhase += p.swaySpeed * dt;
            float sway = (float) Math.sin(p.swayPhase) * 12f;
            p.x += (p.vx + sway) * dt;
            p.y += p.vy * dt;
            p.rotation = (p.rotation + p.rotSpeed * dt) % 360f;

            if (p.y > height + 10 || p.x < -20 || p.x > width + 20) {
                p.x = random.nextFloat() * width;
                p.y = -10 - random.nextFloat() * 20;
                p.vx = (random.nextFloat() - 0.3f) * 8f;
                p.vy = 12f + random.nextFloat() * 18f;
                p.rotSpeed = (random.nextFloat() - 0.5f) * 60f;
                p.size = 2.5f + random.nextFloat() * 2.5f;
                p.colorIdx = random.nextInt(SAKURA_COLORS.length);
            }
        }
    }

    public void render(DrawContext ctx, int ox, int oy, float globalAlpha) {
        for (Petal p : petals) {
            int color = SAKURA_COLORS[p.colorIdx];
            int finalA = (int) (((color >>> 24) & 0xFF) * p.alpha * globalAlpha);
            if (finalA < 4) continue;
            int finalColor = (finalA << 24) | (color & 0x00FFFFFF);
            drawPetal(ctx, ox + p.x, oy + p.y, p.size, p.rotation, finalColor);
        }
    }

    private void drawPetal(DrawContext ctx, float cx, float cy, float size, float rotDeg, int color) {
        float rad = (float) Math.toRadians(rotDeg);

        for (int i = 0; i < 4; i++) {
            float angle = rad + (float)(i * Math.PI / 2.0);
            float ca = (float) Math.cos(angle);
            float sa = (float) Math.sin(angle);

            for (int s = 0; s < 3; s++) {
                float t = (s + 1) / 3f * size;
                int px = (int) (cx + ca * t);
                int py = (int) (cy + sa * t);
                int rad_pixel = s == 0 ? 1 : (s == 1 ? 1 : 0);
                if (rad_pixel >= 0) {
                    ctx.fill(px - rad_pixel, py - rad_pixel,
                             px + rad_pixel + 1, py + rad_pixel + 1, color);
                }
            }
        }

        int centerX = (int) cx;
        int centerY = (int) cy;
        int centerColor = lighten(color, 0.3f);
        ctx.fill(centerX, centerY, centerX + 1, centerY + 1, centerColor);

        if (size > 3.5f) {
            for (int i = 0; i < 4; i++) {
                float angle = rad + (float)(i * Math.PI / 2.0 + Math.PI / 4.0);
                float ca = (float) Math.cos(angle);
                float sa = (float) Math.sin(angle);
                int px = (int) (cx + ca * size * 0.5f);
                int py = (int) (cy + sa * size * 0.5f);
                int alpha = ((color >>> 24) & 0xFF) / 2;
                int dimColor = (alpha << 24) | (color & 0x00FFFFFF);
                ctx.fill(px, py, px + 1, py + 1, dimColor);
            }
        }
    }

    private int lighten(int color, float factor) {
        int a = (color >>> 24) & 0xFF;
        int r = Math.min(255, (int) (((color >>> 16) & 0xFF) * (1 + factor)));
        int g = Math.min(255, (int) (((color >>> 8) & 0xFF) * (1 + factor)));
        int b = Math.min(255, (int) ((color & 0xFF) * (1 + factor)));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
