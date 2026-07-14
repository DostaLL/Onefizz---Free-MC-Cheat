package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.Setting;

/**
 * NoRender — скрытие отвлекающих визуальных эффектов.
 */
public class NoRender extends Module {

    @Setting(name = "Огонь на экране")
    public boolean noFireOverlay = true;

    @Setting(name = "Туман")
    public boolean noFog = true;

    @Setting(name = "Эффект слепоты")
    public boolean noBlindness = true;

    @Setting(name = "Эффект тошноты")
    public boolean noNausea = true;

    @Setting(name = "Вспышка от взрыва")
    public boolean noExplosionFlash = true;

    @Setting(name = "Частицы")
    public boolean noParticles = false;

    @Setting(name = "Дождь / снег")
    public boolean noWeather = false;

    @Setting(name = "Тряска портала")
    public boolean noPortalOverlay = true;

    @Setting(name = "Анимация щита")
    public boolean noShieldBlock = false;

    @Setting(name = "Тряска при уроне (NoHurt)")
    public boolean noHurtCam = true;

    public NoRender() { super("NoRender", "Скрытие огня, тумана, эффектов"); }
}
