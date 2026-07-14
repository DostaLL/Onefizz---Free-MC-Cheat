package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.Setting;

public class Fullbright extends Module {

    @Setting(min = 1f, max = 32f)
    public float gamma = 16.0f;

    public Fullbright() {
        super("Fullbright", "Полное освещение без факелов");
    }
}
