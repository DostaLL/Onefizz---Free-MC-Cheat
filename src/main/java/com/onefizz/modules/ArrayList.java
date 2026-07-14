package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.OneFizzMod;
import com.onefizz.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.Comparator;
import java.util.List;

public class ArrayList extends Module {

    public ArrayList() { super("ArrayList", "Список активных модулей на HUD"); }

    public void onRender(DrawContext ctx) {
        if (!isEnabled()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        var tr = mc.textRenderer;
        int sw = ctx.getScaledWindowWidth();

        List<Module> active = OneFizzMod.modules.getAll().stream()
            .filter(Module::isEnabled)
            .sorted(Comparator.comparingInt((Module m) -> tr.getWidth(m.getName())).reversed())
            .toList();

        int y = 4;
        for (Module m : active) {
            String name = m.getName();
            int w = tr.getWidth(name);
            ctx.drawText(tr, Text.literal(name), sw - w - 4, y, Theme.ALT(), true);
            y += 11;
        }
    }
}
