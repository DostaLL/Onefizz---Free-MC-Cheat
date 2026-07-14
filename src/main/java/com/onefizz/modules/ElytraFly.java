package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * ElytraFly — топ-1 полёт на элитрах с тремя режимами.
 *
 * BOOST  — velocity по вектору взгляда. Простой и быстрый.
 *          Скорость вперёд/назад/вверх/вниз независимы.
 *
 * GLIDE  — плавный полёт: горизонталь по yaw, вертикаль отдельно.
 *          Инерция при отпускании клавиш. Лучший feel.
 *
 * PACKET — спуфинг фейерверка: каждые 10 тиков отправляем
 *          START_FALL_FLYING пакет заново, не давая элитре остановиться.
 *          Velocity управляем напрямую. Лучший обход Grim/AAC.
 */
public class ElytraFly extends Module {

    public enum Mode { BOOST, GLIDE, PACKET }

    @Setting(name = "Режим")
    public Mode  mode          = Mode.GLIDE;

    @Setting(name = "Скорость вперёд", min = 0.01f, max = 5f)
    public float speedForward  = 1.0f;
    @Setting(name = "Скорость назад", min = 0.01f, max = 5f)
    public float speedBack     = 0.5f;
    @Setting(name = "Скорость вбок", min = 0.01f, max = 5f)
    public float speedStrafe   = 0.7f;
    @Setting(name = "Скорость вверх", min = 0.01f, max = 5f)
    public float speedUp       = 0.5f;
    @Setting(name = "Скорость вниз", min = 0.01f, max = 5f)
    public float speedDown     = 0.5f;

    private int  keepAliveTick = 0;
    private int  packetTick    = 0;

    public ElytraFly() { super("ElytraFly", "Полет на элитрах с контролем"); }

    @Override
    protected void onDisable() {
        keepAliveTick = 0;
        packetTick    = 0;
    }

    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null) return;
        var player = mc.player;

        // Проверяем элитру
        var chest = player.getEquippedStack(EquipmentSlot.CHEST);
        if (chest.getItem() != Items.ELYTRA) return;
        if (chest.getDamage() >= chest.getMaxDamage() - 2) { setEnabled(false); return; }

        // Запускаем полёт если не летим
        if (!player.isFallFlying()) {
            if (player.isOnGround()) {
                player.jump();
            } else {
                player.networkHandler.sendPacket(
                    new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            }
            return;
        }

        switch (mode) {
            case BOOST  -> tickBoost(mc);
            case GLIDE  -> tickGlide(mc);
            case PACKET -> tickPacket(mc);
        }

        // Keep-alive для всех режимов
        keepAliveTick++;
        if (keepAliveTick >= 20) {
            keepAliveTick = 0;
            player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(
                player.getX(), player.getY(), player.getZ(),
                player.getYaw(), player.getPitch(), false));
        }
    }

    // ── BOOST ─────────────────────────────────────────────────────────────────
    // Velocity строго по вектору взгляда с раздельными скоростями

    private void tickBoost(MinecraftClient mc) {
        var player = mc.player;
        var opts   = mc.options;
        Vec3d look = player.getRotationVector().normalize();

        double vx = 0, vy = 0, vz = 0;

        if (opts.forwardKey.isPressed()) {
            vx += look.x * speedForward;
            vy += look.y * speedForward;
            vz += look.z * speedForward;
        }
        if (opts.backKey.isPressed()) {
            vx -= look.x * speedBack;
            vy -= look.y * speedBack;
            vz -= look.z * speedBack;
        }

        // Стрейф: перпендикуляр к look в горизонтальной плоскости
        float yaw = (float) Math.toRadians(player.getYaw());
        if (opts.leftKey.isPressed()) {
            vx -= Math.cos(yaw) * speedStrafe;
            vz -= Math.sin(yaw) * speedStrafe;
        }
        if (opts.rightKey.isPressed()) {
            vx += Math.cos(yaw) * speedStrafe;
            vz += Math.sin(yaw) * speedStrafe;
        }

        // Вертикаль переопределяет Y от look
        if (opts.jumpKey.isPressed())  vy =  speedUp;
        if (opts.sneakKey.isPressed()) vy = -speedDown;

        if (vx == 0 && vy == 0 && vz == 0) {
            // Зависаем
            player.setVelocity(player.getVelocity().multiply(0.6, 0, 0.6));
        } else {
            player.setVelocity(vx, vy, vz);
        }
    }

    // ── GLIDE ─────────────────────────────────────────────────────────────────
    // Горизонталь по yaw, вертикаль отдельно, плавная инерция

    private void tickGlide(MinecraftClient mc) {
        var player = mc.player;
        var opts   = mc.options;
        float yaw  = (float) Math.toRadians(player.getYaw());

        Vec3d cur = player.getVelocity();
        double vx = cur.x, vy = cur.y, vz = cur.z;

        boolean anyH = false;
        // Плавный lerp (0.2) — убирает дёрганье
        float smooth = 0.2f;

        if (opts.forwardKey.isPressed()) {
            vx = MathHelper.lerp(smooth, vx, -Math.sin(yaw) * speedForward);
            vz = MathHelper.lerp(smooth, vz,  Math.cos(yaw) * speedForward);
            anyH = true;
        }
        if (opts.backKey.isPressed()) {
            vx = MathHelper.lerp(smooth, vx,  Math.sin(yaw) * speedBack);
            vz = MathHelper.lerp(smooth, vz, -Math.cos(yaw) * speedBack);
            anyH = true;
        }
        if (opts.leftKey.isPressed()) {
            vx = MathHelper.lerp(smooth, vx,  Math.cos(yaw) * speedStrafe);
            vz = MathHelper.lerp(smooth, vz,  Math.sin(yaw) * speedStrafe);
            anyH = true;
        }
        if (opts.rightKey.isPressed()) {
            vx = MathHelper.lerp(smooth, vx, -Math.cos(yaw) * speedStrafe);
            vz = MathHelper.lerp(smooth, vz, -Math.sin(yaw) * speedStrafe);
            anyH = true;
        }

        // Плавное торможение
        if (!anyH) {
            vx *= 0.92;
            vz *= 0.92;
        }

        // Вертикаль — тоже плавнее
        if (opts.jumpKey.isPressed())       vy = MathHelper.lerp(0.2f, (float)vy,  speedUp);
        else if (opts.sneakKey.isPressed()) vy = MathHelper.lerp(0.2f, (float)vy, -speedDown);
        else                                vy *= 0.92;

        player.setVelocity(vx, vy, vz);
    }

    // ── PACKET ────────────────────────────────────────────────────────────────
    // Каждые 20 тиков переотправляем START_FALL_FLYING — элитра не останавливается.
    // Velocity ограничена до 1.5 б/т для обхода Grim.

    private void tickPacket(MinecraftClient mc) {
        var player = mc.player;

        // Управление velocity как в GLIDE
        tickGlide(mc);

        // Ограничиваем общую скорость до 1.5 б/т (Grim кикает выше)
        Vec3d v = player.getVelocity();
        double len = v.length();
        if (len > 1.5) {
            player.setVelocity(v.multiply(1.5 / len));
        }

        // Переотправляем START_FALL_FLYING не чаще чем каждые 20 тиков
        packetTick++;
        if (packetTick >= 20) {
            packetTick = 0;
            player.networkHandler.sendPacket(
                new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }
    }
}
