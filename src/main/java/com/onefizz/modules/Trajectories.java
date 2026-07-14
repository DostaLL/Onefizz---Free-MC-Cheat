package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.Setting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

/**
 * Trajectories — точка приземления снарядов (стрелы, яйца, жемчуг).
 */
public class Trajectories extends Module {

    public enum Mode { BOW, ALL }

    @Setting(name = "Режим")
    public Mode mode = Mode.ALL;

    @Setting(name = "Цвет R", min = 0f, max = 1f)
    public float colorR = 0.3f;
    @Setting(name = "Цвет G", min = 0f, max = 1f)
    public float colorG = 0.8f;
    @Setting(name = "Цвет B", min = 0f, max = 1f)
    public float colorB = 1.0f;

    @Setting(name = "Прозрачность", min = 0.1f, max = 1f)
    public float alpha = 0.7f;

    public Trajectories() { super("Trajectories", "Точка приземления снарядов"); }

    public void onRender(WorldRenderContext ctx) {
        if (!isEnabled()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        PlayerEntity player = mc.player;
        ItemStack stack = player.getMainHandStack();
        if (stack.isEmpty()) stack = player.getOffHandStack();
        if (stack.isEmpty()) return;

        boolean isBow = stack.getItem() instanceof BowItem;
        boolean isThrowable = stack.getItem() instanceof EggItem
                || stack.getItem() instanceof EnderPearlItem
                || stack.getItem() instanceof SnowballItem
                || stack.getItem() instanceof ExperienceBottleItem;

        if (!isBow && !isThrowable) return;
        if (mode == Mode.BOW && !isBow) return;

        float yaw = player.getYaw();
        float pitch = player.getPitch();
        double posX = player.getX();
        double posY = player.getY() + player.getEyeHeight(player.getPose());
        double posZ = player.getZ();

        double motionX = -MathHelper.sin(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
        double motionY = -MathHelper.sin(pitch * 0.017453292F);
        double motionZ = MathHelper.cos(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);

        // Правильная скорость для лука: зависит от натяжения (0-1.0)
        double bowPower = 1.0;
        if (isBow && player.isUsingItem()) {
            int useTicks = player.getItemUseTime();
            bowPower = getBowPower(useTicks);
        }
        
        double speed = isBow ? (1.0 + 3.0 * bowPower) : 1.5;
        motionX *= speed;
        motionY *= speed;
        motionZ *= speed;

        // Гравитация и сопротивление
        double gravity = isBow ? 0.05 : 0.03;
        double drag = isBow ? 0.99 : 0.99;

        // Simulate trajectory
        double lastX = posX, lastY = posY, lastZ = posZ;
        for (int i = 0; i < 200; i++) {
            double nextX = lastX + motionX;
            double nextY = lastY + motionY;
            double nextZ = lastZ + motionZ;

            if (mc.world.getBlockCollisions(null, net.minecraft.util.math.Box.of(
                    new Vec3d(nextX, nextY, nextZ), 0.1, 0.1, 0.1)).iterator().hasNext()) {
                drawLandingPoint(ctx, nextX, nextY, nextZ);
                return;
            }

            motionY -= gravity;
            motionX *= drag;
            motionY *= drag;
            motionZ *= drag;

            lastX = nextX;
            lastY = nextY;
            lastZ = nextZ;
            
            // Stop if falling too slow (hit ground essentially)
            if (motionY < 0.01 && nextY < posY - 0.5) break;
        }

        // If no collision, draw at end point
        drawLandingPoint(ctx, lastX, lastY, lastZ);
    }
    
    private double getBowPower(int useTicks) {
        // Minecraft's bow power calculation
        float f = (float)useTicks / 20.0f;
        f = (f * f + f * 2.0f) / 3.0f;
        if (f > 1.0f) f = 1.0f;
        return f;
    }

    private void drawLandingPoint(WorldRenderContext ctx, double x, double y, double z) {
        MinecraftClient mc = MinecraftClient.getInstance();
        MatrixStack ms = ctx.matrixStack();
        ms.push();
        Vec3d cam = ctx.camera().getPos();
        ms.translate(x - cam.x, y - cam.y, z - cam.z);
        Matrix4f mat = ms.peek().getPositionMatrix();

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        int r = (int)(colorR * 255);
        int g = (int)(colorG * 255);
        int b = (int)(colorB * 255);
        int a = (int)(alpha * 255);
        int color = (a << 24) | (r << 16) | (g << 8) | b;

        float s = 0.25f;
        // Bottom face
        buf.vertex(mat, -s, 0.02f, -s).color(r, g, b, a);
        buf.vertex(mat, s, 0.02f, -s).color(r, g, b, a);
        buf.vertex(mat, s, 0.02f, s).color(r, g, b, a);
        buf.vertex(mat, -s, 0.02f, s).color(r, g, b, a);

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        BufferRenderer.drawWithGlobalProgram(buf.end());
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();

        ms.pop();
    }
}
