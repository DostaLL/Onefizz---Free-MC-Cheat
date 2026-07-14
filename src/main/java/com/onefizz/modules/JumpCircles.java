package com.onefizz.modules;

import com.mojang.blaze3d.systems.RenderSystem;
import com.onefizz.BoxRenderer;
import com.onefizz.EspRenderLayers;
import com.onefizz.Module;
import com.onefizz.Setting;
import com.onefizz.Theme;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class JumpCircles extends Module {

    @Setting(min = 0.4f, max = 2f)
    public float radius = 0.9f;

    @Setting(name = "Скорость вращения", min = 10f, max = 120f)
    public float rotateSpeed = 40f;

    @Setting(min = 0.5f, max = 3f)
    public float duration = 1.2f;

    private float angle = 0f;
    private boolean wasOnGround = true;
    private long animStartMs = 0;
    private long lastTimeMs = 0;

    public JumpCircles() { super("JumpCircles", "Круги при прыжке (косметика)"); }

    public void onRender(WorldRenderContext ctx) {
        if (!isEnabled()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        PlayerEntity player = mc.player;

        long now = System.currentTimeMillis();
        float dtSec = lastTimeMs == 0 ? 0f : (now - lastTimeMs) / 1000f;
        lastTimeMs = now;

        boolean onGround = player.isOnGround();
        if (wasOnGround && !onGround) {
            animStartMs = now;
        }
        wasOnGround = onGround;

        if (animStartMs == 0) return;
        float elapsed = (now - animStartMs) / 1000f;
        if (elapsed > duration) return;

        float t = elapsed / duration;
        float scale;
        if (t < 0.3f) {
            float p = t / 0.3f;
            scale = 1f - (1f - p) * (1f - p) * (1f - p);
        } else if (t > 0.8f) {
            float p = (t - 0.8f) / 0.2f;
            scale = 1f - p;
        } else {
            scale = 1f;
        }
        float alpha;
        if (t < 0.2f) alpha = t / 0.2f;
        else if (t > 0.7f) alpha = 1f - (t - 0.7f) / 0.3f;
        else alpha = 1f;
        alpha = Math.max(0f, Math.min(1f, alpha));

        angle = (angle + rotateSpeed * dtSec) % 360f;

        float td = ctx.tickCounter().getTickDelta(false);
        Vec3d cam = ctx.camera().getPos();
        double px = MathHelper.lerp(td, player.prevX, player.getX());
        double py = MathHelper.lerp(td, player.prevY, player.getY());
        double pz = MathHelper.lerp(td, player.prevZ, player.getZ());

        MatrixStack ms = ctx.matrixStack();
        VertexConsumerProvider.Immediate imm = mc.getBufferBuilders().getEntityVertexConsumers();

        float r = radius * scale;
        float a = alpha;

        float cr = Theme.get().accentR();
        float cg = Theme.get().accentG();
        float cb = Theme.get().accentB();

        BoxRenderer.drawThroughWalls(() -> {
            RenderSystem.lineWidth(2.5f);

            ms.push();
            ms.translate(px - cam.x, py - cam.y + 0.05, pz - cam.z);

            // Draw outer circle
            drawCircle(ms, imm, r, a, cr, cg, cb, 48);
            // Draw middle inner circle
            drawCircle(ms, imm, r * 0.5f, a, cr, cg, cb, 32);
            // Draw small inner circle
            drawCircle(ms, imm, r * 0.3f, a, cr, cg, cb, 24);
            // Draw pentagram (5-pointed star)
            drawPentagram(ms, imm, r * 0.85f, a, cr, cg, cb);
            // Draw dots at pentagram vertices
            drawPentagramDots(ms, imm, r * 0.85f, a, cr, cg, cb);
            imm.draw();

            ms.pop();
            RenderSystem.lineWidth(1.0f);
        });
    }

    private void drawPentagram(MatrixStack ms, VertexConsumerProvider.Immediate imm,
                                float r, float alpha, float cr, float cg, float cb) {
        VertexConsumer vc = imm.getBuffer(EspRenderLayers.LINES_THROUGH_WALLS);
        Matrix4f mat = ms.peek().getPositionMatrix();
        var nm = ms.peek();

        // 5 vertices of a regular pentagon, connect every other (star pattern: 0-2-4-1-3-0)
        float[] px = new float[5], pz = new float[5];
        for (int i = 0; i < 5; i++) {
            float ang = (float) Math.toRadians(angle + i * 72.0 - 90.0);
            px[i] = MathHelper.cos(ang) * r;
            pz[i] = MathHelper.sin(ang) * r;
        }
        int[] order = {0, 2, 4, 1, 3, 0};
        for (int i = 0; i < 5; i++) {
            float x1 = px[order[i]], z1 = pz[order[i]];
            float x2 = px[order[i+1]], z2 = pz[order[i+1]];
            float dx = x2-x1, dz = z2-z1;
            float len = (float) Math.sqrt(dx*dx+dz*dz);
            float nx = len > 0 ? dx/len : 0f;
            float nz = len > 0 ? dz/len : 0f;
            vc.vertex(mat, x1, 0, z1).color(cr, cg, cb, alpha).normal(nm, nx, 0, nz);
            vc.vertex(mat, x2, 0, z2).color(cr, cg, cb, alpha).normal(nm, nx, 0, nz);
        }
    }

    private void drawPentagramDots(MatrixStack ms, VertexConsumerProvider.Immediate imm,
                                    float r, float alpha, float cr, float cg, float cb) {
        VertexConsumer vc = imm.getBuffer(EspRenderLayers.LINES_THROUGH_WALLS);
        Matrix4f mat = ms.peek().getPositionMatrix();
        var nm = ms.peek();

        float dotSize = 0.03f;
        for (int i = 0; i < 5; i++) {
            float ang = (float) Math.toRadians(angle + i * 72.0 - 90.0);
            float cx = MathHelper.cos(ang) * r;
            float cz = MathHelper.sin(ang) * r;
            // Draw small cross as dot
            vc.vertex(mat, cx - dotSize, 0, cz).color(cr, cg, cb, alpha).normal(nm, 1, 0, 0);
            vc.vertex(mat, cx + dotSize, 0, cz).color(cr, cg, cb, alpha).normal(nm, 1, 0, 0);
            vc.vertex(mat, cx, 0, cz - dotSize).color(cr, cg, cb, alpha).normal(nm, 0, 0, 1);
            vc.vertex(mat, cx, 0, cz + dotSize).color(cr, cg, cb, alpha).normal(nm, 0, 0, 1);
        }
    }

    private void drawCircle(MatrixStack ms, VertexConsumerProvider.Immediate imm,
                             float r, float alpha, float cr, float cg, float cb, int segments) {
        VertexConsumer vc = imm.getBuffer(EspRenderLayers.LINES_THROUGH_WALLS);
        Matrix4f mat = ms.peek().getPositionMatrix();
        var nm = ms.peek();

        for (int i = 0; i < segments; i++) {
            float a1 = (float) Math.toRadians((float) i / segments * 360f);
            float a2 = (float) Math.toRadians((float)(i + 1) / segments * 360f);
            float x1 = MathHelper.cos(a1) * r, z1 = MathHelper.sin(a1) * r;
            float x2 = MathHelper.cos(a2) * r, z2 = MathHelper.sin(a2) * r;
            float dx = x2-x1, dz = z2-z1;
            float len = (float) Math.sqrt(dx*dx+dz*dz);
            float nx = len > 0 ? dx/len : 0f;
            float nz = len > 0 ? dz/len : 0f;
            vc.vertex(mat, x1, 0, z1).color(cr, cg, cb, alpha * 0.6f).normal(nm, nx, 0, nz);
            vc.vertex(mat, x2, 0, z2).color(cr, cg, cb, alpha * 0.6f).normal(nm, nx, 0, nz);
        }
    }
}
