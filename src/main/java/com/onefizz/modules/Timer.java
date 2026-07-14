package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.Setting;

public class Timer extends Module {

    @Setting(min = 0.1f, max = 10f)
    public float speed = 2.0f;

    public Timer() { super("Timer", "Ускорение игрового времени"); }
}
