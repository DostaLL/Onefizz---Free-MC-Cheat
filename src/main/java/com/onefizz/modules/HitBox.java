package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.Setting;

/**
 * HitBox — расширяет bounding box у других игроков для упрощения попадания.
 * Чисто клиентское — сервер не видит и не флагит.
 */
public class HitBox extends Module {

    @Setting(min = 0f, max = 1.5f)
    public float expand = 0.3f;

    @Setting public boolean affectPlayers = true;
    @Setting public boolean affectMobs = false;

    public HitBox() { super("HitBox", "Увеличение хитбоксов врагов"); }
}
