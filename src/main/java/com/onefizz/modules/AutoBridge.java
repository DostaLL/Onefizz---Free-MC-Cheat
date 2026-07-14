package com.onefizz.modules;

import com.onefizz.AntiLagback;
import com.onefizz.Module;
import com.onefizz.Setting;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class AutoBridge extends Module {

    public enum Mode { NORMAL, SAFE }

    @Setting(name = "Режим") public Mode mode = Mode.SAFE;
    @Setting(name = "Скорость", min = 0.05f, max = 0.2f) public float speed = 0.12f;
    @Setting(name = "Авто-сник") public boolean autoSneak = true;
    @Setting(name = "Задержка (тики)", min = 0f, max = 5f) public int delay = 1;
    @Setting(name = "AntiLagback") public boolean antiLagback = true;

    private int tickCounter = 0;
    private boolean active = false;
    private BlockPos lastPlacedPos = null;
    private int waitForConfirm = 0;

    public AutoBridge() { super("AutoBridge", "Автомост для BedWars"); }

    @Override
    protected void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null && active) {
            mc.options.sneakKey.setPressed(false);
            mc.options.backKey.setPressed(false);
            active = false;
        }
        lastPlacedPos = null;
        waitForConfirm = 0;
    }

    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null || mc.world == null || mc.interactionManager == null) return;
        var player = mc.player;

        if (!(player.getInventory().getMainHandStack().getItem() instanceof BlockItem)) {
            if (active) {
                mc.options.backKey.setPressed(false);
                mc.options.sneakKey.setPressed(false);
                active = false;
            }
            return;
        }

        if (antiLagback && AntiLagback.isSuppressed()) {
            if (active) { mc.options.backKey.setPressed(false); mc.options.sneakKey.setPressed(false); }
            tickCounter = 0;
            return;
        }

        active = true;

        if (autoSneak) {
            BlockPos under = player.getBlockPos().down();
            boolean overEdge = mc.world.getBlockState(under).isReplaceable();
            mc.options.sneakKey.setPressed(overEdge);
        }

        if (mode == Mode.SAFE) {
            mc.options.backKey.setPressed(true);
        } else {
            float yawRad = (float) Math.toRadians(player.getYaw());
            double backX = Math.sin(yawRad) * speed;
            double backZ = -Math.cos(yawRad) * speed;
            if (mc.options.leftKey.isPressed())  { backX += Math.cos(yawRad) * speed * 0.5; backZ += Math.sin(yawRad) * speed * 0.5; }
            if (mc.options.rightKey.isPressed()) { backX -= Math.cos(yawRad) * speed * 0.5; backZ -= Math.sin(yawRad) * speed * 0.5; }
            player.setVelocity(backX, player.getVelocity().y, backZ);
        }

        // SAFE: ждём подтверждения предыдущего блока
        if (mode == Mode.SAFE && lastPlacedPos != null) {
            BlockState placed = mc.world.getBlockState(lastPlacedPos);
            if (placed.isReplaceable() || placed.isAir()) {
                waitForConfirm++;
                if (waitForConfirm < 8) return;
            }
            lastPlacedPos = null;
            waitForConfirm = 0;
        }

        int effectiveDelay = mode == Mode.SAFE ? Math.max(delay, 2) : delay;
        tickCounter++;
        if (tickCounter <= effectiveDelay) return;
        tickCounter = 0;

        // Определяем цель: блок под ногами или впереди
        Direction front = Direction.fromRotation(player.getYaw());
        BlockPos targetPos = player.getBlockPos().down().offset(front);
        if (!mc.world.getBlockState(targetPos).isReplaceable()) {
            targetPos = player.getBlockPos().down();
            if (!mc.world.getBlockState(targetPos).isReplaceable()) return;
        }

        // Проверка: блок виден в прицеле?
        BlockHitResult crosshairHit = raycastForBlock(mc, targetPos);
        if (crosshairHit == null) return;

        BlockHitResult hit = new BlockHitResult(crosshairHit.getPos(), crosshairHit.getSide(), crosshairHit.getBlockPos(), false);
        var result = mc.interactionManager.interactBlock(player, Hand.MAIN_HAND, hit);
        if (result.isAccepted()) {
            player.swingHand(Hand.MAIN_HAND);
            if (mode == Mode.SAFE) {
                lastPlacedPos = targetPos;
                waitForConfirm = 0;
            }
        }
    }

    private BlockHitResult raycastForBlock(MinecraftClient mc, BlockPos target) {
        var player = mc.player;
        Vec3d eyes = player.getEyePos();
        Vec3d center = Vec3d.ofCenter(target);

        // Проверяем все 6 граней блока — ищем ту, что видна
        for (Direction dir : Direction.values()) {
            Vec3d hitVec = center.add(dir.getOffsetX() * 0.5, dir.getOffsetY() * 0.5, dir.getOffsetZ() * 0.5);
            Vec3d dirToHit = hitVec.subtract(eyes).normalize();
            double dist = eyes.distanceTo(hitVec);

            var raycastResult = player.raycast(dist + 0.5, 0, false);
            if (raycastResult instanceof BlockHitResult bhr
                && bhr.getBlockPos().equals(target.offset(dir.getOpposite()))
                && bhr.getSide() == dir) {
                return new BlockHitResult(hitVec, dir, target.offset(dir.getOpposite()), false);
            }
        }
        return null;
    }
}
