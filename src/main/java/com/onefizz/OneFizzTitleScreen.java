package com.onefizz;

import imgui.*;
import imgui.flag.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class OneFizzTitleScreen extends Screen {

    public OneFizzTitleScreen() { super(Text.literal("OneFizz")); }
    @Override protected void init() { ImGuiRenderer.ensureInitialized(); }
    @Override public boolean shouldCloseOnEsc() { return false; }

    @Override public boolean mouseClicked(double mx, double my, int btn) { ImGui.getIO().setMousePos((float) mx, (float) my); ImGui.getIO().setMouseDown(btn, true); if (btn == 0 && !ImGui.getIO().getWantCaptureMouse()) click(mx, my); return true; }
    @Override public boolean mouseReleased(double mx, double my, int btn) { ImGui.getIO().setMousePos((float) mx, (float) my); ImGui.getIO().setMouseDown(btn, false); return true; }
    @Override public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) { ImGui.getIO().setMousePos((float) mx, (float) my); return true; }
    @Override public boolean mouseScrolled(double mx, double my, double h, double v) { ImGui.getIO().setMouseWheel(ImGui.getIO().getMouseWheel() + (float) v); return true; }
    @Override public boolean keyPressed(int k, int s, int m) { ImGui.getIO().setKeysDown(k, true); return true; }
    @Override public boolean keyReleased(int k, int s, int m) { ImGui.getIO().setKeysDown(k, false); return true; }
    @Override public boolean charTyped(char c, int m) { ImGui.getIO().addInputCharacter(c); return true; }

    private void click(double mx, double my) {
        float bw = 200, bh = 28, gap = 8, bx = (width - bw) / 2f, by0 = height / 2f + 10;
        for (int i = 0; i < 4; i++) { float by = by0 + i * (bh + gap); if (mx >= bx && mx < bx + bw && my >= by && my < by + bh) { switch (i) { case 0 -> client.setScreen(new SelectWorldScreen(this)); case 1 -> client.setScreen(new MultiplayerScreen(this)); case 2 -> client.setScreen(new OptionsScreen(this, client.options)); case 3 -> client.scheduleStop(); } } }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ImGuiRenderer.beginFrame(delta);
        ImGui.getIO().setMousePos(mouseX, mouseY);

        ImGui.setNextWindowPos(0, 0, ImGuiCond.Always);
        ImGui.setNextWindowSize(width, height, ImGuiCond.Always);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 0f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, 0f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0, 0);

        int f = ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoBringToFrontOnFocus | ImGuiWindowFlags.NoNavFocus | ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse;
        ImGui.begin("Title", f);

        ImDrawList d = ImGui.getWindowDrawList();
        d.addRectFilledMultiColor(0, 0, width, height, 0xFF080612, 0xFF080612, 0xFF04020A, 0xFF04020A);
        d.addLine(0, 0, width * 0.4f, 0, 0xFF2563EB, 2f);
        d.addLine(width * 0.6f, height - 2, width, height - 2, 0xFF60A5FA, 2f);

        float logoY = height / 2f - 70;
        String title = "ONEFIZZ"; float ts = 3f, tw = ImGui.calcTextSize(title).x * ts;
        d.addText(null, 24 * ts, (width - tw) / 2f, logoY, 0xFFFFFFFF, title);
        String sub = "by IMaska"; float sw = ImGui.calcTextSize(sub).x;
        d.addText(null, 16f, (width - sw) / 2f, logoY + 38, 0x665B6789, sub);

        float ay = logoY + 54, aw = 150, ax = (width - aw) / 2f;
        d.addLine(ax, ay, ax + aw, ay, 0xFF2563EB, 2f);
        d.addCircleFilled(ax, ay, 2, 0xFF60A5FA, 0);
        d.addCircleFilled(ax + aw, ay, 2, 0xFF60A5FA, 0);

        String[] btns = {"Singleplayer", "Multiplayer", "Settings", "Quit"};
        float bw = 200, bh = 28, gap = 8, bx = (width - bw) / 2f, by0 = height / 2f + 10;
        for (int i = 0; i < 4; i++) { float by = by0 + i * (bh + gap); boolean hover = mouseX >= bx && mouseX < bx + bw && mouseY >= by && mouseY < by + bh; int bg = hover ? 0xFF1E3A8A : 0xCC0F1228, border = hover ? 0xFF4A6CF7 : 0xFF1E2468; d.addRectFilled(bx, by, bx + bw, by + bh, bg, 7, 15); d.addRect(bx, by, bx + bw, by + bh, border, 7, 15, 1f); if (hover) d.addRectFilled(bx + 2, by + 4, bx + 5, by + bh - 4, 0xFF60A5FA, 2, 15); float lw = ImGui.calcTextSize(btns[i]).x; d.addText(null, 17f, bx + (bw - lw) / 2f, by + (bh - 17) / 2f, hover ? 0xFFFFFFFF : 0xBBCCD0E0, btns[i]); }

        String ver = "OneFizz Client | MC " + MinecraftClient.getInstance().getGameVersion();
        d.addText(null, 14f, 6, height - 14, 0x55608199, ver);
        String hint = "RShift - in-game menu";
        d.addText(null, 14f, width - ImGui.calcTextSize(hint).x - 6, height - 14, 0x332563EB, hint);

        int cnt = UserCounter.getUserCount();
        if (cnt >= 0) { String ct = "Users: " + cnt; d.addText(null, 14f, 6, height - 28, 0x664A6CF7, ct); }
        else if (UserCounter.isConfigured()) { d.addText(null, 14f, 6, height - 28, 0x44608199, "Users: ..."); }

        ImGui.end();
        ImGui.popStyleVar(3);
        ImGuiRenderer.endFrame();
    }
}
