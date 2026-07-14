package com.onefizz;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import org.joml.Matrix4f;

public final class BoxRenderer {

    public static void drawEdges(MatrixStack matrices, VertexConsumerProvider vcp,
                                  Box box, float r, float g, float b, float a) {
        VertexConsumer vc = vcp.getBuffer(EspRenderLayers.LINES_THROUGH_WALLS);
        Matrix4f mat = matrices.peek().getPositionMatrix();

        float x0 = (float) box.minX, y0 = (float) box.minY, z0 = (float) box.minZ;
        float x1 = (float) box.maxX, y1 = (float) box.maxY, z1 = (float) box.maxZ;

        int[][] edges = {
            {0,0,0,1,0,0},{1,0,0,1,0,1},{1,0,1,0,0,1},{0,0,1,0,0,0},
            {0,1,0,1,1,0},{1,1,0,1,1,1},{1,1,1,0,1,1},{0,1,1,0,1,0},
            {0,0,0,0,1,0},{1,0,0,1,1,0},{1,0,1,1,1,1},{0,0,1,0,1,1}
        };
        var nm = matrices.peek();
        for (int[] e : edges) {
            float ax = e[0] == 0 ? x0 : x1, ay = e[1] == 0 ? y0 : y1, az = e[2] == 0 ? z0 : z1;
            float bx = e[3] == 0 ? x0 : x1, by = e[4] == 0 ? y0 : y1, bz = e[5] == 0 ? z0 : z1;
            float dx = bx - ax, dy = by - ay, dz = bz - az;
            float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (len > 0) { dx /= len; dy /= len; dz /= len; }
            vc.vertex(mat, ax, ay, az).color(r, g, b, a).normal(nm, dx, dy, dz);
            vc.vertex(mat, bx, by, bz).color(r, g, b, a).normal(nm, dx, dy, dz);
        }
    }

    public static void drawEdges(MatrixStack matrices, VertexConsumerProvider vcp,
                                  Box box, float r, float g, float b) {
        drawEdges(matrices, vcp, box, r, g, b, 1f);
    }

    public static void drawFaces(MatrixStack matrices, VertexConsumerProvider vcp,
                                  Box box, float r, float g, float b, float aTop, float aBot) {
        VertexConsumer vc = vcp.getBuffer(EspRenderLayers.QUADS_THROUGH_WALLS);
        Matrix4f mat = matrices.peek().getPositionMatrix();

        float x0 = (float) box.minX, y0 = (float) box.minY, z0 = (float) box.minZ;
        float x1 = (float) box.maxX, y1 = (float) box.maxY, z1 = (float) box.maxZ;

        // Bottom
        vc.vertex(mat, x0, y0, z0).color(r, g, b, aBot);
        vc.vertex(mat, x1, y0, z0).color(r, g, b, aBot);
        vc.vertex(mat, x1, y0, z1).color(r, g, b, aBot);
        vc.vertex(mat, x0, y0, z1).color(r, g, b, aBot);
        // Top
        vc.vertex(mat, x0, y1, z0).color(r, g, b, aTop);
        vc.vertex(mat, x0, y1, z1).color(r, g, b, aTop);
        vc.vertex(mat, x1, y1, z1).color(r, g, b, aTop);
        vc.vertex(mat, x1, y1, z0).color(r, g, b, aTop);
        // -X
        vc.vertex(mat, x0, y0, z0).color(r, g, b, aBot);
        vc.vertex(mat, x0, y0, z1).color(r, g, b, aBot);
        vc.vertex(mat, x0, y1, z1).color(r, g, b, aTop);
        vc.vertex(mat, x0, y1, z0).color(r, g, b, aTop);
        // +X
        vc.vertex(mat, x1, y0, z0).color(r, g, b, aBot);
        vc.vertex(mat, x1, y1, z0).color(r, g, b, aTop);
        vc.vertex(mat, x1, y1, z1).color(r, g, b, aTop);
        vc.vertex(mat, x1, y0, z1).color(r, g, b, aBot);
        // -Z
        vc.vertex(mat, x0, y0, z0).color(r, g, b, aBot);
        vc.vertex(mat, x0, y1, z0).color(r, g, b, aTop);
        vc.vertex(mat, x1, y1, z0).color(r, g, b, aTop);
        vc.vertex(mat, x1, y0, z0).color(r, g, b, aBot);
        // +Z
        vc.vertex(mat, x0, y0, z1).color(r, g, b, aBot);
        vc.vertex(mat, x1, y0, z1).color(r, g, b, aBot);
        vc.vertex(mat, x1, y1, z1).color(r, g, b, aTop);
        vc.vertex(mat, x0, y1, z1).color(r, g, b, aTop);
    }

    /**
     * Wrapper for draw calls. Now mostly redundant since EspRenderLayers
     * already include depth=ALWAYS, but kept for API compatibility.
     */
    public static void drawThroughWalls(Runnable drawCall) {
        drawCall.run();
    }
}
