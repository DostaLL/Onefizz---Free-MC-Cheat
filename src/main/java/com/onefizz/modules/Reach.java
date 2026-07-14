package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.Setting;

/**
 * Reach — увеличивает дистанцию атаки и взаимодействия с блоками.
 * Vanilla limit: 3.0 атака, 4.5 блоки. Многие сервера разрешают до 6 на атаку.
 */
public class Reach extends Module {

    @Setting(min = 3f, max = 8f)
    public float attackReach = 4.5f;

    @Setting(min = 4.5f, max = 8f)
    public float blockReach = 5.5f;

    public Reach() { super("Reach", "Увеличение дальности ударов"); }
}
