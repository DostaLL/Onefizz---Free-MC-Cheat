package com.onefizz.modules;

import com.mojang.blaze3d.systems.RenderSystem;
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

/**
 * ChinaHat — рисует конус-шляпу прямо на голове игрока.
 *
 * Высота головы стандартного игрока: глаза на 1.62, голова на ~1.8.
 * Шляпа сидит ровно на голове: основание Y=1.8, вершина Y=1.8+height.
 *
 * Настройки: цвет (custom или Theme), толщина линий, высота, радиус,
 * количество сегментов, на всех игроках или только на себе.
 */
public class ChinaHat extends Module {

    public enum ColorMode { THEME, CUSTOM }
    public enum Targets { SELF, OTHERS, ALL }

    @Setting(name = "Цвет") public ColorMode colorMode = ColorMode.THEME;
    @Setting(name = "На ком") public Targets targets = Targets.ALL;

    @Setting(name = "Толщина линий", min = 1f, max = 5f)
    public float lineWidth = 2f;

    @Setting(name = "Высота", min = 0.2f, max = 2f)
    public float height = 0.7f;

    @Setting(name = "Радиус", min = 0.2f, max = 1.5f)
    public float radius = 0.45f;

    @Setting(name = "Сегменты", min = 6f, max = 32f)
    public int segments = 16;

    @Setting(name = "Прозрачность", min = 0.1f, max = 1f)
    public float opacity = 0.85f;

    @Setting(name = "Свечение")
    public boolean glow = true;

    // Custom color (RGB через @Setting)
    @Setting(name = "R (custom)", min = 0f, max = 1f) public float r = 1.0f;
    @Setting(name = "G (custom)", min = 0f, max = 1f) public float g = 0.3f;
    @Setting(name = "B (custom)", min = 0f, max = 1f) public float b = 0.3f;

    public ChinaHat() { super("ChinaHat", "Визуальная шляпа над головой"); }

    public void onRender(WorldRenderContext ctx) {
        if (!isEnabled()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        Vec3d camPos = ctx.camera().getPos();
        float tickDelta = ctx.tickCounter().getTickDelta(false);
        MatrixStack matrices = ctx.matrixStack();
        VertexConsumerProvider.Immediate immediate = mc.getBufferBuilders().getEntityVertexConsumers();

        // Цвет
        float cr, cg, cb;
        if (colorMode == ColorMode.CUSTOM) {
            cr = r; cg = g; cb = b;
        } else {
            cr = Theme.get().accentR();
            cg = Theme.get().accentG();
            cb = Theme.get().accentB();
        }

        RenderSystem.lineWidth(lineWidth);

        // Рендерим на нужных игроках
        for (var player : mc.world.getPlayers()) {
            if (targets == Targets.SELF && player != mc.player) continue;
            if (targets == Targets.OTHERS && player == mc.player) continue;

            renderHat(player, matrices, immediate, camPos, tickDelta, cr, cg, cb);
        }

        immediate.draw(EspRenderLayers.LINES_THROUGH_WALLS);
        RenderSystem.lineWidth(1.0f);
    }

    private void renderHat(PlayerEntity player, MatrixStack matrices,
                            VertexConsumerProvider.Immediate immediate,
                            Vec3d camPos, float tickDelta,
                            float cr, float cg, float cb) {
        double px = MathHelper.lerp(tickDelta, player.prevX, player.getX());
        double py = MathHelper.lerp(tickDelta, player.prevY, player.getY());
        double pz = MathHelper.lerp(tickDelta, player.prevZ, player.getZ());

        // Высота головы зависит от позы (стандарт 1.8, sneak 1.5, swim/elytra 0.6)
        float headY = player.getStandingEyeHeight() + 0.18f; // глаза + 18см до макушки
        if (player.isSneaking()) headY -= 0.1f;

        VertexConsumer vc = immediate.getBuffer(EspRenderLayers.LINES_THROUGH_WALLS);
        var mat = matrices.peek().getPositionMatrix();
        var nm = matrices.peek();

        matrices.push();
        matrices.translate(px - camPos.x, py - camPos.y, pz - camPos.z);
        mat = matrices.peek().getPositionMatrix();
        nm = matrices.peek();

        float baseY = headY;
        float tipY = headY + height;
        float a = opacity;

        // Основание (круг)
        for (int i = 0; i < segments; i++) {
            float angle1 = (float)(2 * Math.PI * i / segments);
            float angle2 = (float)(2 * Math.PI * ((i + 1) % segments) / segments);
            float x1 = MathHelper.cos(angle1) * radius, z1 = MathHelper.sin(angle1) * radius;
            float x2 = MathHelper.cos(angle2) * radius, z2 = MathHelper.sin(angle2) * radius;

            // Линия от вершины к точке круга
            vc.vertex(mat, 0, tipY, 0).color(cr, cg, cb, a).normal(nm, 0, 1, 0);
            vc.vertex(mat, x1, baseY, z1).color(cr, cg, cb, a).normal(nm, 0, 1, 0);

            // Линия по кругу основания
            vc.vertex(mat, x1, baseY, z1).color(cr, cg, cb, a).normal(nm, 0, 1, 0);
            vc.vertex(mat, x2, baseY, z2).color(cr, cg, cb, a).normal(nm, 0, 1, 0);
        }

        // Glow: дополнительный полупрозрачный слой большего радиуса
        if (glow) {
            float gr = radius * 1.15f;
            float gtipY = tipY + 0.03f;
            float ga = a * 0.3f;
            for (int i = 0; i < segments; i++) {
                float angle1 = (float)(2 * Math.PI * i / segments);
                float angle2 = (float)(2 * Math.PI * ((i + 1) % segments) / segments);
                float x1 = MathHelper.cos(angle1) * gr, z1 = MathHelper.sin(angle1) * gr;
                float x2 = MathHelper.cos(angle2) * gr, z2 = MathHelper.sin(angle2) * gr;

                vc.vertex(mat, 0, gtipY, 0).color(cr, cg, cb, ga).normal(nm, 0, 1, 0);
                vc.vertex(mat, x1, baseY - 0.05f, z1).color(cr, cg, cb, ga).normal(nm, 0, 1, 0);
                vc.vertex(mat, x1, baseY - 0.05f, z1).color(cr, cg, cb, ga).normal(nm, 0, 1, 0);
                vc.vertex(mat, x2, baseY - 0.05f, z2).color(cr, cg, cb, ga).normal(nm, 0, 1, 0);
            }
        }

        matrices.pop();
    }
}
