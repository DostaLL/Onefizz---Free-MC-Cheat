package com.onefizz.modules;

import com.onefizz.Module;

/**
 * NoHurt — отключает тряску камеры при получении урона.
 * Работает через миксин NoHurtCamMixin на GameRenderer#tiltViewWhenHurt.
 */
public class NoHurt extends Module {
    public NoHurt() { super("NoHurt", "Отключение тряски камеры от урона"); }
}
