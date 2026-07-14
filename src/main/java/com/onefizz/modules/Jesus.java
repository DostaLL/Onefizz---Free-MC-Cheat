package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * Jesus — стабильное хождение по воде.
 *
 * Режимы:
 * - SOLID: вода ведет себя как твердый блок (Mixin)
 * - MOTION: точный контроль velocity и позиции, без тряски
 * - DOLPHIN: быстрое плавание
 */
public class Jesus extends Module {

    public enum Mode { SOLID, MOTION, DOLPHIN }

    @Setting(name = "Режим") public Mode mode = Mode.MOTION;
    @Setting(name = "Скорость дельфина", min = 0.5f, max = 3.0f) public float dolphinSpeed = 1.5f;

    public Jesus() { super("Jesus", "Хождение по воде как по суше"); }

    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null || mc.world == null) return;
        switch (mode) {
            case SOLID  -> tickSolid(mc);
            case MOTION -> tickMotion(mc);
            case DOLPHIN -> tickDolphin(mc);
        }
    }

    /** MOTION — точный контроль, никакой тряски */
    private void tickMotion(MinecraftClient mc) {
        var player = mc.player;
        if (mc.options.sneakKey.isPressed()) return; // игрок хочет нырнуть

        BlockPos feetPos = player.getBlockPos();
        FluidState feetFluid = mc.world.getFluidState(feetPos);
        FluidState headFluid = mc.world.getFluidState(BlockPos.ofFloored(player.getEyePos()));
        FluidState belowFluid = mc.world.getFluidState(feetPos.down());

        boolean inWater = isWater(feetFluid) || isWater(belowFluid);
        boolean headInWater = isWater(headFluid);

        if (!inWater) return;

        Vec3d vel = player.getVelocity();
        double waterY = getWaterSurfaceY(mc, feetPos);

        if (headInWater) {
            // Полностью под водой — плавно всплываем
            player.setVelocity(vel.x, 0.08, vel.z);
        } else {
            // На поверхности — "прилипаем" к ней
            double playerFeetY = player.getY();
            double diff = waterY - playerFeetY;

            if (diff > 0.05) {
                // Ниже поверхности — подтягиваем вверх
                player.setVelocity(vel.x, 0.06, vel.z);
            } else if (diff < -0.05) {
                // Выше поверхности — опускаем (не должно происходить)
                player.setVelocity(vel.x, -0.02, vel.z);
            } else {
                // Идеально на поверхности — Y velocity = 0, стабильность
                player.setVelocity(vel.x, 0.0, vel.z);
                // Микро-коррекция позиции для идеальной ровности
                if (Math.abs(diff) > 0.001) {
                    player.setPosition(player.getX(), waterY, player.getZ());
                }
            }

            // Отправляем onGround=true для античита (вода как твердая поверхность)
            if (mc.player.age % 4 == 0) {
                player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
            }
        }
    }

    /** SOLID — коллизия с водой как с блоком (обрабатывается JesusMixin) */
    private void tickSolid(MinecraftClient mc) {
        var player = mc.player;
        if (mc.options.sneakKey.isPressed()) return;

        BlockPos feetPos = player.getBlockPos();
        if (isWater(mc.world.getFluidState(feetPos)) || isWater(mc.world.getFluidState(feetPos.down()))) {
            Vec3d vel = player.getVelocity();
            if (vel.y < 0) {
                player.setVelocity(vel.x, 0, vel.z);
            }
        }
    }

    /** DOLPHIN — быстрое плавание под водой */
    private void tickDolphin(MinecraftClient mc) {
        var player = mc.player;
        if (!player.isTouchingWater()) return;

        Vec3d vel = player.getVelocity();
        float yaw = (float) Math.toRadians(player.getYaw());
        float pitch = (float) Math.toRadians(player.getPitch());

        double speed = 0.04 * dolphinSpeed;
        double mx = -Math.sin(yaw) * Math.cos(pitch) * speed;
        double my = -Math.sin(pitch) * speed;
        double mz = Math.cos(yaw) * Math.cos(pitch) * speed;

        if (mc.options.forwardKey.isPressed()) {
            player.setVelocity(vel.x + mx, vel.y + my, vel.z + mz);
        } else {
            if (vel.y < -0.02) player.setVelocity(vel.x, 0.02, vel.z);
        }
    }

    /** Возвращает Y-координату поверхности воды в блоке */
    private double getWaterSurfaceY(MinecraftClient mc, BlockPos pos) {
        FluidState fluid = mc.world.getFluidState(pos);
        if (isWater(fluid)) {
            return pos.getY() + fluid.getHeight(mc.world, pos);
        }
        FluidState below = mc.world.getFluidState(pos.down());
        if (isWater(below)) {
            return pos.down().getY() + below.getHeight(mc.world, pos.down());
        }
        return pos.getY();
    }

    public boolean isSolidMode() {
        return isEnabled() && mode == Mode.SOLID;
    }

    private boolean isWater(FluidState fs) {
        return fs.getFluid() == Fluids.WATER || fs.getFluid() == Fluids.FLOWING_WATER;
    }
}
