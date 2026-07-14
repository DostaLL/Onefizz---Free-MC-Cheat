package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class StaffListModule extends Module {

    @Setting(name = "Staff count") public String staffCount = "0";
    @Setting(name = "Interval (sec)", min = 5f, max = 120f) public int intervalSec = 30;

    private int timer = 0;

    public StaffListModule() { super("StaffList", "Reports staff presence in chat"); }

    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null || mc.world == null) return;

        if (timer > 0) {
            timer--;
            return;
        }

        String text = "[OneFizz] \u041d\u0430 \u0441\u0435\u0440\u0432\u0435\u0440\u0435 \u0437\u0430\u043c\u0435\u0447\u0435\u043d\u043e ("
            + staffCount + ") \u043c\u043e\u0434\u0435\u0440\u0430\u0442\u043e\u0440\u043e\u0432/\u0430\u0434\u043c\u0438\u043d\u0438\u0441\u0442\u0440\u0430\u0442\u043e\u0440\u043e\u0432";
        mc.player.networkHandler.sendChatMessage(text);
        timer = intervalSec * 20;
    }

    @Override
    protected void onEnable() {
        timer = 0;
    }

    @Override
    protected void onDisable() {
        timer = 0;
    }
}
