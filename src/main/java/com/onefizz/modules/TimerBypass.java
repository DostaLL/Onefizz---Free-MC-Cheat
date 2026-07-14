package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.Setting;

import java.util.Random;

/**
 * TimerBypass — продвинутый таймер с обходом timer detection.
 *
 * Большинство античитов проверяют game speed через анализ packet timing.
 * Если ты постоянно отправляешь пакеты быстрее 20 TPS — детект.
 *
 * Этот модуль:
 * - Постепенно увеличивает скорость (не резко)
 * - Периодически возвращает скорость к 1.0 чтобы сбросить prediction
 * - Рандомизирует значение в малом диапазоне
 *
 * Применяется через TimerMixin (уже существует) — этот модуль предоставляет
 * текущий multiplier через getMultiplier().
 */
public class TimerBypass extends Module {

    @Setting(name = "Целевая скорость", min = 1.0f, max = 3.0f)
    public float targetSpeed = 1.5f;

    @Setting(name = "Плавный разгон")
    public boolean smoothRamp = true;

    @Setting(name = "Сброс каждые N тиков", min = 20f, max = 200f)
    public int resetEvery = 60;

    @Setting(name = "Рандомизация")
    public boolean randomize = true;

    private float currentMultiplier = 1.0f;
    private int tickCounter = 0;
    private final Random random = new Random();

    public TimerBypass() { super("TimerBypass", "Обход лимита таймера"); }

    @Override
    protected void onEnable() {
        currentMultiplier = 1.0f;
        tickCounter = 0;
    }

    @Override
    protected void onDisable() {
        currentMultiplier = 1.0f;
    }

    public void onTick(net.minecraft.client.MinecraftClient mc) {
        if (!isEnabled()) {
            currentMultiplier = 1.0f;
            return;
        }

        tickCounter++;

        // Периодический сброс к 1.0 чтобы сбить prediction
        if (tickCounter >= resetEvery) {
            tickCounter = 0;
            currentMultiplier = 1.0f;
            return;
        }

        // Плавный разгон
        if (smoothRamp) {
            float diff = targetSpeed - currentMultiplier;
            currentMultiplier += diff * 0.1f;
        } else {
            currentMultiplier = targetSpeed;
        }

        // Рандомизация ±5%
        if (randomize) {
            currentMultiplier *= (0.95f + random.nextFloat() * 0.10f);
        }
    }

    public float getMultiplier() {
        return isEnabled() ? currentMultiplier : 1.0f;
    }
}
