package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.Setting;

/**
 * FastPlace — устраняет задержку между расстановкой блоков.
 * Логика в FastPlaceMixin.
 */
public class FastPlace extends Module {

    @Setting(min = 0f, max = 4f)
    public int delay = 0; // ticks — 0 = мгновенно, 4 = ванильно

    public FastPlace() { super("FastPlace", "Ускоренная постановка блоков"); }
}
