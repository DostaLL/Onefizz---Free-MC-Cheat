package com.onefizz.modules;

import com.onefizz.BoxRenderer;
import com.onefizz.Module;
import com.onefizz.Setting;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class MobESP extends Module {

    @Setting
    public boolean showHostile = true;

    @Setting
    public boolean showPassive = false;

    @Setting(min = 8f, max = 128f)
    public float distance = 64f;

    public MobESP() { super("MobESP", "Подсветка мобов сквозь стены"); }

    public void onRender(WorldRenderContext ctx) {
        if (!isEnabled()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        MatrixStack matrices = ctx.matrixStack();
        VertexConsumerProvider.Immediate immediate = mc.getBufferBuilders().getEntityVertexConsumers();
        Vec3d camPos = ctx.camera().getPos();
        float tickDelta = ctx.tickCounter().getTickDelta(false);

        BoxRenderer.drawThroughWalls(() -> {
            mc.world.getEntitiesByClass(LivingEntity.class,
                mc.player.getBoundingBox().expand(distance),
                e -> true).forEach(entity -> {
                boolean hostile = entity instanceof HostileEntity;
                boolean passive = entity instanceof PassiveEntity;
                if (!hostile && !passive) return;
                if (hostile && !showHostile) return;
                if (passive && !showPassive) return;
                if (mc.player.distanceTo(entity) > distance || entity == mc.player) return;

                float r = hostile ? 1f : 0f;
                float g = hostile ? 0.2f : 0.8f;
                float b = 0.2f;

                double px = MathHelper.lerp(tickDelta, entity.prevX, entity.getX());
                double py = MathHelper.lerp(tickDelta, entity.prevY, entity.getY());
                double pz = MathHelper.lerp(tickDelta, entity.prevZ, entity.getZ());

                matrices.push();
                matrices.translate(px - camPos.x, py - camPos.y, pz - camPos.z);
                Box box = entity.getBoundingBox().offset(-px, -py, -pz);
                BoxRenderer.drawEdges(matrices, immediate, box, r, g, b, 0.9f);
                matrices.pop();
            });
            immediate.draw();
        });
    }
}
