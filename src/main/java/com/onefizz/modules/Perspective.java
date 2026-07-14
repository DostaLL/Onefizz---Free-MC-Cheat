package com.onefizz.modules;

import com.onefizz.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;

/**
 * Perspective (Free Look) — позволяет крутить камерой в F5,
 * при этом сохраняя направление движения/строительства/атаки.
 *
 * Работает через @Redirect на ClientPlayerEntity#changeLookDirection:
 * дельта мыши идёт в cameraYaw/cameraPitch вместо player.yaw/pitch.
 * Камера через миксин показывает camera rotation.
 */
public class Perspective extends Module {

    public static float cameraYaw = 0f;
    public static float cameraPitch = 0f;
    public static float realYaw = 0f;
    public static float realPitch = 0f;

    private static Perspective INSTANCE;

    public Perspective() {
        super("Perspective", "Свободная камера в F5-режиме");
        INSTANCE = this;
    }

    public static boolean isActive() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || INSTANCE == null) return false;
        return INSTANCE.isEnabled() && !mc.options.getPerspective().isFirstPerson();
    }

    @Override
    protected void onEnable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            realYaw = mc.player.getYaw();
            realPitch = mc.player.getPitch();
            cameraYaw = realYaw;
            cameraPitch = realPitch;
        }
    }

    @Override
    protected void onDisable() {
        // Возвращаем camera к направлению игрока
        cameraYaw = realYaw;
        cameraPitch = realPitch;
    }

    /** Вызывается из PerspectiveMouseMixin при движении мыши */
    public static void onMouseDelta(double deltaX, double deltaY) {
        cameraYaw = (float) (cameraYaw + deltaX);
        cameraPitch = (float) (cameraPitch + deltaY);
        cameraPitch = MathHelper.clamp(cameraPitch, -90f, 90f);
    }
}
