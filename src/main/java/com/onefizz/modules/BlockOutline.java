package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.Setting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

/**
 * BlockOutline — цветная подсветка блока на который смотришь.
 * Заменяет стандартный чёрный контур.
 */
public class BlockOutline extends Module {

    @Setting(name = "Цвет R", min = 0f, max = 1f)
    public float colorR = 0.3f;
    @Setting(name = "Цвет G", min = 0f, max = 1f)
    public float colorG = 0.8f;
    @Setting(name = "Цвет B", min = 0f, max = 1f)
    public float colorB = 1.0f;

    @Setting(name = "Прозрачность", min = 0.1f, max = 1f)
    public float alpha = 0.9f;

    @Setting(name = "Толщина", min = 1f, max = 5f)
    public float width = 2f;

    @Setting(name = "Заливка")
    public boolean fill = false;

    public BlockOutline() { super("BlockOutline", "Цветной контур блока под прицелом"); }

    public void onRender(WorldRenderContext ctx) {
        if (!isEnabled()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult hit = (BlockHitResult) mc.crosshairTarget;
        BlockPos pos = hit.getBlockPos();
        if (mc.world.isAir(pos)) return;

        Box box = mc.world.getBlockState(pos).getOutlineShape(mc.world, pos).getBoundingBox().offset(pos);
        Vec3d cam = ctx.camera().getPos();

        MatrixStack ms = ctx.matrixStack();
        ms.push();
        ms.translate(-cam.x, -cam.y, -cam.z);

        int r = (int)(colorR * 255);
        int g = (int)(colorG * 255);
        int b = (int)(colorB * 255);
        int a = (int)(alpha * 255);

        if (fill) {
            drawFilledBox(ms, box, r, g, b, (int)(alpha * 80));
        }
        drawBoxOutline(ms, box, r, g, b, a, width);

        ms.pop();
    }

    private void drawBoxOutline(MatrixStack ms, Box box, int r, int g, int b, int a, float lineWidth) {
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        Matrix4f mat = ms.peek().getPositionMatrix();

        float minX = (float) box.minX, minY = (float) box.minY, minZ = (float) box.minZ;
        float maxX = (float) box.maxX, maxY = (float) box.maxY, maxZ = (float) box.maxZ;

        // Bottom
        line(buf, mat, minX, minY, minZ, maxX, minY, minZ, r, g, b, a);
        line(buf, mat, maxX, minY, minZ, maxX, minY, maxZ, r, g, b, a);
        line(buf, mat, maxX, minY, maxZ, minX, minY, maxZ, r, g, b, a);
        line(buf, mat, minX, minY, maxZ, minX, minY, minZ, r, g, b, a);
        // Top
        line(buf, mat, minX, maxY, minZ, maxX, maxY, minZ, r, g, b, a);
        line(buf, mat, maxX, maxY, minZ, maxX, maxY, maxZ, r, g, b, a);
        line(buf, mat, maxX, maxY, maxZ, minX, maxY, maxZ, r, g, b, a);
        line(buf, mat, minX, maxY, maxZ, minX, maxY, minZ, r, g, b, a);
        // Vertical edges
        line(buf, mat, minX, minY, minZ, minX, maxY, minZ, r, g, b, a);
        line(buf, mat, maxX, minY, minZ, maxX, maxY, minZ, r, g, b, a);
        line(buf, mat, maxX, minY, maxZ, maxX, maxY, maxZ, r, g, b, a);
        line(buf, mat, minX, minY, maxZ, minX, maxY, maxZ, r, g, b, a);

        RenderSystem.enableDepthTest();
        RenderSystem.lineWidth(lineWidth);
        BufferRenderer.drawWithGlobalProgram(buf.end());
        RenderSystem.lineWidth(1f);
    }

    private void drawFilledBox(MatrixStack ms, Box box, int r, int g, int b, int a) {
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        Matrix4f mat = ms.peek().getPositionMatrix();

        float x0 = (float) box.minX, y0 = (float) box.minY, z0 = (float) box.minZ;
        float x1 = (float) box.maxX, y1 = (float) box.maxY, z1 = (float) box.maxZ;

        // Bottom
        quad(buf, mat, x0,y0,z0, x1,y0,z0, x1,y0,z1, x0,y0,z1, r, g, b, a);
        // Top
        quad(buf, mat, x0,y1,z0, x0,y1,z1, x1,y1,z1, x1,y1,z0, r, g, b, a);
        // Front
        quad(buf, mat, x0,y0,z1, x1,y0,z1, x1,y1,z1, x0,y1,z1, r, g, b, a);
        // Back
        quad(buf, mat, x1,y0,z0, x0,y0,z0, x0,y1,z0, x1,y1,z0, r, g, b, a);
        // Left
        quad(buf, mat, x0,y0,z0, x0,y0,z1, x0,y1,z1, x0,y1,z0, r, g, b, a);
        // Right
        quad(buf, mat, x1,y0,z1, x1,y0,z0, x1,y1,z0, x1,y1,z1, r, g, b, a);

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        BufferRenderer.drawWithGlobalProgram(buf.end());
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private static void line(BufferBuilder buf, Matrix4f mat,
                             float x1, float y1, float z1, float x2, float y2, float z2,
                             int r, int g, int b, int a) {
        buf.vertex(mat, x1, y1, z1).color(r, g, b, a);
        buf.vertex(mat, x2, y2, z2).color(r, g, b, a);
    }

    private static void quad(BufferBuilder buf, Matrix4f mat,
                             float x1, float y1, float z1, float x2, float y2, float z2,
                             float x3, float y3, float z3, float x4, float y4, float z4,
                             int r, int g, int b, int a) {
        buf.vertex(mat, x1, y1, z1).color(r, g, b, a);
        buf.vertex(mat, x2, y2, z2).color(r, g, b, a);
        buf.vertex(mat, x3, y3, z3).color(r, g, b, a);
        buf.vertex(mat, x4, y4, z4).color(r, g, b, a);
    }
}
