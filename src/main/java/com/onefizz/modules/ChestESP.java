package com.onefizz.modules;

import com.onefizz.BoxRenderer;
import com.onefizz.Module;
import com.onefizz.Setting;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.entity.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class ChestESP extends Module {

    @Setting
    public boolean showChest = true;

    @Setting
    public boolean showTrappedChest = true;

    @Setting
    public boolean showEnderChest = true;

    @Setting
    public boolean showBarrel = true;

    @Setting
    public boolean showShulker = true;

    @Setting(min = 8f, max = 128f)
    public float distance = 64f;

    public ChestESP() { super("ChestESP", "Подсветка сундуков сквозь стены"); }

    public void onRender(WorldRenderContext ctx) {
        if (!isEnabled()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        MatrixStack matrices = ctx.matrixStack();
        VertexConsumerProvider.Immediate immediate = mc.getBufferBuilders().getEntityVertexConsumers();
        Vec3d camPos = ctx.camera().getPos();

        var chunkManager = mc.world.getChunkManager();
        int pcx = mc.player.getBlockX() >> 4;
        int pcz = mc.player.getBlockZ() >> 4;
        int radius = (int) (distance / 16) + 2;

        BoxRenderer.drawThroughWalls(() -> {
            for (int cx = pcx - radius; cx <= pcx + radius; cx++) {
                for (int cz = pcz - radius; cz <= pcz + radius; cz++) {
                    var chunk = chunkManager.getWorldChunk(cx, cz);
                    if (chunk == null) continue;
                    for (var be : chunk.getBlockEntities().values()) {
                        if (!shouldShow(be)) continue;
                        BlockPos pos = be.getPos();
                        if (mc.player.squaredDistanceTo(Vec3d.ofCenter(pos)) > distance * distance) continue;

                        float[] color = getColor(be);
                        double rx = pos.getX() - camPos.x;
                        double ry = pos.getY() - camPos.y;
                        double rz = pos.getZ() - camPos.z;

                        matrices.push();
                        matrices.translate(rx, ry, rz);
                        BoxRenderer.drawEdges(matrices, immediate, new Box(0, 0, 0, 1, 1, 1), color[0], color[1], color[2], 1f);
                        matrices.pop();
                    }
                }
            }
            immediate.draw();
        });
    }

    private boolean shouldShow(BlockEntity be) {
        return (be instanceof ChestBlockEntity && showChest)
            || (be instanceof TrappedChestBlockEntity && showTrappedChest)
            || (be instanceof EnderChestBlockEntity && showEnderChest)
            || (be instanceof BarrelBlockEntity && showBarrel)
            || (be instanceof ShulkerBoxBlockEntity && showShulker);
    }

    private float[] getColor(BlockEntity be) {
        if (be instanceof EnderChestBlockEntity)   return new float[]{0.6f, 0f, 0.8f};
        if (be instanceof TrappedChestBlockEntity) return new float[]{1f, 0.3f, 0f};
        if (be instanceof ShulkerBoxBlockEntity)   return new float[]{0.8f, 0.4f, 0.8f};
        if (be instanceof BarrelBlockEntity)       return new float[]{0.6f, 0.4f, 0.2f};
        return new float[]{1f, 0.85f, 0f};
    }
}
