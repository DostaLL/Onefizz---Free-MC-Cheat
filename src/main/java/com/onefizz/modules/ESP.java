package com.onefizz.modules;

import com.onefizz.BoxRenderer;
import com.onefizz.Module;
import com.onefizz.Setting;
import com.onefizz.EspRenderLayers;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Matrix4f;

import java.util.List;

public class ESP extends Module {

    public enum BoxStyle { FULL, OUTLINE, CORNERS }

    @Setting(name = "Дистанция", min = 8f, max = 128f)
    public float distance = 64f;

    @Setting(name = "Показать бокс")
    public boolean showBox = true;

    @Setting(name = "Стиль бокса")
    public BoxStyle boxStyle = BoxStyle.FULL;

    @Setting(name = "Показать линии")
    public boolean showLines = true;

    @Setting(name = "Показать ник")
    public boolean showNick = true;

    @Setting(name = "Показать здоровье")
    public boolean showHealth = true;

    @Setting(name = "Показать броню")
    public boolean showArmor = true;

    @Setting(name = "Цвет R", min = 0f, max = 1f)
    public float colorR = 168f / 255f;

    @Setting(name = "Цвет G", min = 0f, max = 1f)
    public float colorG = 85f / 255f;

    @Setting(name = "Цвет B", min = 0f, max = 1f)
    public float colorB = 247f / 255f;

    @Setting(name = "Прозрачность", min = 0f, max = 1f)
    public float boxOpacity = 0.85f;

    @Setting(name = "Толщина линий", min = 1f, max = 5f)
    public float lineWidth = 2f;

    public ESP() { super("ESP", "Подсветка игроков сквозь стены"); }

    public void onRender(WorldRenderContext ctx) {
        if (!isEnabled()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        MatrixStack matrices = ctx.matrixStack();
        VertexConsumerProvider.Immediate immediate = mc.getBufferBuilders().getEntityVertexConsumers();
        Vec3d camPos = ctx.camera().getPos();
        float tickDelta = ctx.tickCounter().getTickDelta(false);

        var players = mc.world.getPlayers().stream()
                .filter(p -> p != mc.player && !p.isDead())
                .filter(p -> mc.player.distanceTo(p) <= distance)
                .toList();

        BoxRenderer.drawThroughWalls(() -> {
            RenderSystem.lineWidth(lineWidth);

            for (PlayerEntity player : players) {
                double px = MathHelper.lerp(tickDelta, player.prevX, player.getX());
                double py = MathHelper.lerp(tickDelta, player.prevY, player.getY());
                double pz = MathHelper.lerp(tickDelta, player.prevZ, player.getZ());

                double rx = px - camPos.x;
                double ry = py - camPos.y;
                double rz = pz - camPos.z;

                if (showBox) {
                    matrices.push();
                    matrices.translate(rx, ry, rz);
                    Box box = player.getBoundingBox().offset(-px, -py, -pz);

                    switch (boxStyle) {
                        case FULL -> {
                            BoxRenderer.drawFaces(matrices, immediate, box,
                                colorR, colorG, colorB, 0.18f, 0.05f);
                            BoxRenderer.drawEdges(matrices, immediate, box, colorR, colorG, colorB, boxOpacity);
                        }
                        case OUTLINE -> {
                            BoxRenderer.drawEdges(matrices, immediate, box, colorR, colorG, colorB, boxOpacity);
                        }
                        case CORNERS -> {
                            drawCorners(matrices, immediate, box, colorR, colorG, colorB, boxOpacity);
                        }
                    }
                    matrices.pop();
                }

                if (showLines) {
                    matrices.push();
                    Vec3d mid = new Vec3d(rx, ry + player.getHeight() / 2, rz);
                    VertexConsumer vc = immediate.getBuffer(EspRenderLayers.LINES_THROUGH_WALLS);
                    var mat = matrices.peek().getPositionMatrix();
                    var nm = matrices.peek();
                    float fx = (float) mid.x, fy = (float) mid.y, fz = (float) mid.z;
                    float len = (float) mid.length();
                    float nx = len > 0 ? fx / len : 0f;
                    float ny = len > 0 ? fy / len : 1f;
                    float nz = len > 0 ? fz / len : 0f;
                    vc.vertex(mat, 0, 0, 0).color(colorR, colorG, colorB, 0.6f).normal(nm, nx, ny, nz);
                    vc.vertex(mat, fx, fy, fz).color(colorR, colorG, colorB, 0.6f).normal(nm, nx, ny, nz);
                    matrices.pop();
                }

                if (showNick || showHealth || showArmor) {
                    renderLabels(mc, matrices, immediate, player, px, py, pz, camPos);
                }
            }
            immediate.draw();
            RenderSystem.lineWidth(1.0f);
        });
    }

    private void drawCorners(MatrixStack matrices, VertexConsumerProvider vcp,
                              Box box, float r, float g, float b, float a) {
        VertexConsumer vc = vcp.getBuffer(EspRenderLayers.LINES_THROUGH_WALLS);
        Matrix4f mat = matrices.peek().getPositionMatrix();
        var nm = matrices.peek();

        float x0 = (float) box.minX, y0 = (float) box.minY, z0 = (float) box.minZ;
        float x1 = (float) box.maxX, y1 = (float) box.maxY, z1 = (float) box.maxZ;
        float cornerLen = (x1 - x0) * 0.25f;
        float cornerH = (y1 - y0) * 0.25f;

        // 8 corners, each with 3 short edges
        float[][] corners = {
            {x0,y0,z0}, {x1,y0,z0}, {x1,y0,z1}, {x0,y0,z1},
            {x0,y1,z0}, {x1,y1,z0}, {x1,y1,z1}, {x0,y1,z1}
        };
        float[][] dirs = {
            {1,0,0, 0,1,0, 0,0,1},  {-1,0,0, 0,1,0, 0,0,1},
            {-1,0,0, 0,1,0, 0,0,-1}, {1,0,0, 0,1,0, 0,0,-1},
            {1,0,0, 0,-1,0, 0,0,1}, {-1,0,0, 0,-1,0, 0,0,1},
            {-1,0,0, 0,-1,0, 0,0,-1}, {1,0,0, 0,-1,0, 0,0,-1}
        };

        for (int i = 0; i < 8; i++) {
            float cx = corners[i][0], cy = corners[i][1], cz = corners[i][2];
            float[] d = dirs[i];
            // X edge
            line(vc, mat, nm, cx, cy, cz, cx + d[0]*cornerLen, cy, cz, r, g, b, a);
            // Y edge
            line(vc, mat, nm, cx, cy, cz, cx, cy + d[3]*cornerH, cz, r, g, b, a);
            // Z edge
            line(vc, mat, nm, cx, cy, cz, cx, cy, cz + d[6]*cornerLen, r, g, b, a);
        }
    }

    private static void line(VertexConsumer vc, Matrix4f mat, MatrixStack.Entry nm,
                              float x1, float y1, float z1, float x2, float y2, float z2,
                              float r, float g, float b, float a) {
        float dx = x2-x1, dy = y2-y1, dz = z2-z1;
        float len = (float) Math.sqrt(dx*dx+dy*dy+dz*dz);
        if (len > 0) { dx/=len; dy/=len; dz/=len; }
        vc.vertex(mat, x1, y1, z1).color(r, g, b, a).normal(nm, dx, dy, dz);
        vc.vertex(mat, x2, y2, z2).color(r, g, b, a).normal(nm, dx, dy, dz);
    }

    private void renderLabels(MinecraftClient mc, MatrixStack matrices,
                               VertexConsumerProvider vcp, PlayerEntity player,
                               double px, double py, double pz, Vec3d camPos) {
        TextRenderer tr = mc.textRenderer;
        double dist = mc.player.distanceTo(player);
        float scale = Math.max(0.02f, Math.min(0.08f, 0.025f * (float)(dist / 10.0)));

        matrices.push();
        matrices.translate(px - camPos.x, py - camPos.y + player.getHeight() + 0.4, pz - camPos.z);
        matrices.multiply(mc.getEntityRenderDispatcher().getRotation());
        matrices.scale(-scale, -scale, scale);

        int yOff = 0;

        if (showHealth) {
            float hp = player.getHealth();
            float maxHp = player.getMaxHealth();
            float pct = Math.max(0f, Math.min(1f, hp / maxHp));
            int barW = 40;
            int barH = 4;
            int barX = -barW / 2;
            int barY = yOff;
            VertexConsumer fc = vcp.getBuffer(RenderLayer.getDebugQuads());
            var mat = matrices.peek().getPositionMatrix();
            fc.vertex(mat, barX,        barY,        0).color(0f, 0f, 0f, 0.55f);
            fc.vertex(mat, barX,        barY + barH, 0).color(0f, 0f, 0f, 0.55f);
            fc.vertex(mat, barX + barW, barY + barH, 0).color(0f, 0f, 0f, 0.55f);
            fc.vertex(mat, barX + barW, barY,        0).color(0f, 0f, 0f, 0.55f);
            float fr = pct < 0.5f ? 1f : (1f - (pct - 0.5f) * 2f);
            float fg = pct < 0.5f ? pct * 2f : 1f;
            int fillW = (int)(barW * pct);
            fc.vertex(mat, barX,            barY,        0).color(fr, fg, 0.1f, 0.95f);
            fc.vertex(mat, barX,            barY + barH, 0).color(fr, fg, 0.1f, 0.95f);
            fc.vertex(mat, barX + fillW,    barY + barH, 0).color(fr, fg, 0.1f, 0.95f);
            fc.vertex(mat, barX + fillW,    barY,        0).color(fr, fg, 0.1f, 0.95f);
            yOff += barH + 3;
        }

        if (showNick) {
            String nick = player.getName().getString();
            int w = tr.getWidth(nick);
            ctxFill(vcp, matrices, -w/2 - 2, yOff - 1, w/2 + 2, yOff + 9, 0x99000000);
            tr.draw(nick, -w / 2f, yOff, 0xFFE9D5FF, true,
                    matrices.peek().getPositionMatrix(), vcp,
                    TextRenderer.TextLayerType.SEE_THROUGH, 0, 0xF000F0);
            yOff += 11;
        }

        if (showHealth) {
            String hp = String.format("%.0f / %.0f", player.getHealth(), player.getMaxHealth());
            int w = tr.getWidth(hp);
            tr.draw(hp, -w / 2f, yOff, 0xFFFFFFFF, false,
                    matrices.peek().getPositionMatrix(), vcp,
                    TextRenderer.TextLayerType.SEE_THROUGH, 0, 0xF000F0);
            yOff += 10;
        }
        if (showArmor) {
            String ar = "Armor " + player.getArmor();
            int w = tr.getWidth(ar);
            tr.draw(ar, -w / 2f, yOff, 0xFFA39BB7, false,
                    matrices.peek().getPositionMatrix(), vcp,
                    TextRenderer.TextLayerType.SEE_THROUGH, 0, 0xF000F0);
        }

        matrices.pop();
    }

    private static void ctxFill(VertexConsumerProvider vcp, MatrixStack ms,
                                 int x1, int y1, int x2, int y2, int color) {
        VertexConsumer fc = vcp.getBuffer(RenderLayer.getDebugQuads());
        var mat = ms.peek().getPositionMatrix();
        float a = ((color >>> 24) & 0xFF) / 255f;
        float r = ((color >>> 16) & 0xFF) / 255f;
        float g = ((color >>> 8)  & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        fc.vertex(mat, x1, y1, 0).color(r, g, b, a);
        fc.vertex(mat, x1, y2, 0).color(r, g, b, a);
        fc.vertex(mat, x2, y2, 0).color(r, g, b, a);
        fc.vertex(mat, x2, y1, 0).color(r, g, b, a);
    }
}
