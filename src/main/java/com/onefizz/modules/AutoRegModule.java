package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class AutoRegModule extends Module {

    @Setting(name = "Password") public String password = "123777";
    @Setting(name = "Repeat (sec)", min = 1f, max = 30f) public int repeatSec = 5;

    private int timer = 0;
    private boolean sentOnce = false;

    public AutoRegModule() { super("AutoReg", "Auto register on cracked servers"); }

    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null || mc.world == null) return;

        if (timer > 0) {
            timer--;
            return;
        }

        String cmd = "/register " + password + " " + password;
        mc.player.networkHandler.sendChatCommand("register " + password + " " + password);
        mc.player.sendMessage(Text.literal("\u00a7b[AutoReg] \u00a7fSent register command"), true);
        sentOnce = true;
        timer = repeatSec * 20;
    }

    @Override
    protected void onEnable() {
        timer = 0;
        sentOnce = false;
    }

    @Override
    protected void onDisable() {
        timer = 0;
    }
}
