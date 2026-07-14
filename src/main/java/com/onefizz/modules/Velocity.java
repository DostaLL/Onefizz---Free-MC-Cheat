package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

/**
 * Velocity — уменьшает нокбэк с рандомизацией и задержкой.
 * horizontal/vertical = процент ОСТАВШЕГОСЯ нокбэка (5-20% = сильное снижение).
 * Delay 1-2 тика после получения пакета обходит Grim velocity-A.
 */
public class Velocity extends Module {

    @Setting(min = 0f, max = 100f)
    public float horizontal = 12f;

    @Setting(min = 0f, max = 100f)
    public float vertical = 15f;

    @Setting
    public boolean cancelExplosions = true;

    // Внутреннее состояние для delayed velocity
    private Vec3d pendingVelocity = null;
    private int delayTicks = 0;
    private final Random random = new Random();

    public Velocity() { super("Velocity", "Редукция отбрасывания ударами"); }

    /**
     * Вызывается из VelocityMixin при получении velocity пакета.
     * Сохраняет модифицированный velocity для применения через 1-2 тика.
     */
    public void scheduleVelocityModify(Vec3d originalVelocity) {
        float hMul = (horizontal + random.nextFloat() * 5f - 2.5f) / 100f;
        float vMul = (vertical + random.nextFloat() * 5f - 2.5f) / 100f;
        hMul = Math.max(0f, Math.min(1f, hMul));
        vMul = Math.max(0f, Math.min(1f, vMul));

        pendingVelocity = new Vec3d(
            originalVelocity.x * hMul,
            originalVelocity.y * vMul,
            originalVelocity.z * hMul
        );
        delayTicks = 1 + random.nextInt(2); // 1-2 тика задержки
    }

    /**
     * Вызывается каждый тик из OneFizzMod.
     */
    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null) return;
        if (pendingVelocity == null) return;

        delayTicks--;
        if (delayTicks <= 0) {
            mc.player.setVelocity(pendingVelocity);
            pendingVelocity = null;
        }
    }

    @Override
    protected void onDisable() {
        pendingVelocity = null;
        delayTicks = 0;
    }
}
