package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.Setting;

public class Step extends Module {

    @Setting(name = "Высота", min = 0.6f, max = 2.5f)
    public float height = 1.0f;

    public Step() { super("Step", "Автоподъем на блоки без прыжка"); }
}
