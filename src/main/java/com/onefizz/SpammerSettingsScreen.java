package com.onefizz;

import com.onefizz.modules.Spammer;
import imgui.*;
import imgui.flag.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class SpammerSettingsScreen extends Screen {

    private static final int W = 440, H = 310;
    private final Screen parent;
    private final Spammer spammer;
    private int gx, gy;
    private boolean dragging;
    private int dragOffX, dragOffY;

    public SpammerSettingsScreen(Screen parent, Spammer spammer) { super(Text.literal("Spammer")); this.parent = parent; this.spammer = spammer; }
    @Override protected void init() { gx = (width - W) / 2; gy = (height - H) / 2; ImGuiRenderer.ensureInitialized(); }
    @Override public boolean shouldPause() { return false; }
    @Override public void close() { spammer.saveMessages(); client.setScreen(parent); }

    @Override public boolean mouseClicked(double mx, double my, int btn) { ImGuiIO io = ImGui.getIO(); io.setMousePos((float) mx, (float) my); io.setMouseDown(btn, true); int rx = (int) mx - gx, ry = (int) my - gy; if (rx >= 0 && rx < W && ry >= 0 && ry < 26 && btn == 0) { dragging = true; dragOffX = rx; dragOffY = ry; return true; } return io.getWantCaptureMouse() || super.mouseClicked(mx, my, btn); }
    @Override public boolean mouseReleased(double mx, double my, int btn) { dragging = false; ImGui.getIO().setMousePos((float) mx, (float) my); ImGui.getIO().setMouseDown(btn, false); return super.mouseReleased(mx, my, btn); }
    @Override public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) { if (dragging) { gx = (int) mx - dragOffX; gy = (int) my - dragOffY; return true; } ImGui.getIO().setMousePos((float) mx, (float) my); return super.mouseDragged(mx, my, btn, dx, dy); }
    @Override public boolean mouseScrolled(double mx, double my, double h, double v) { ImGui.getIO().setMouseWheel(ImGui.getIO().getMouseWheel() + (float) v); return super.mouseScrolled(mx, my, h, v); }
    @Override public boolean keyPressed(int k, int s, int m) { ImGui.getIO().setKeysDown(k, true); if (ImGui.getIO().getWantCaptureKeyboard()) return true; if (k == GLFW.GLFW_KEY_ESCAPE) { close(); return true; } return super.keyPressed(k, s, m); }
    @Override public boolean keyReleased(int k, int s, int m) { ImGui.getIO().setKeysDown(k, false); return super.keyReleased(k, s, m); }
    @Override public boolean charTyped(char c, int m) { ImGui.getIO().addInputCharacter(c); return super.charTyped(c, m); }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ImGuiRenderer.beginFrame(delta);
        ImGui.getIO().setMousePos(mouseX, mouseY);

        ImGui.setNextWindowPos(gx, gy, ImGuiCond.Always);
        ImGui.setNextWindowSize(W, H, ImGuiCond.Always);

        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 10f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, 0f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0, 0);
        ImGui.pushStyleColor(ImGuiCol.WindowBg, 0xFF0B0C1E);

        int fl = ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoBringToFrontOnFocus | ImGuiWindowFlags.NoNavFocus | ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse;
        ImGui.begin("Spammer", fl);

        ImDrawList d = ImGui.getWindowDrawList();
        float wx = ImGui.getWindowPosX(), wy = ImGui.getWindowPosY(), ww = ImGui.getWindowWidth(), wh = ImGui.getWindowHeight();

        d.addRectFilled(wx, wy, wx + ww, wy + wh, 0xFF0B0C1E, 10, 15);
        d.addRectFilled(wx + 6, wy, wx + ww - 6, wy + 26, 0xFF0F1030, 10, 3);
        d.addLine(wx + 10, wy + 26, wx + ww - 10, wy + 26, 0xFF2563EB, 2f);
        d.addText(wx + 12, wy + 6, 0xFFFFFFFF, "Spammer  -  " + spammer.messages.size() + " msg");

        boolean ch = ImGui.isMouseHoveringRect(wx + ww - 24, wy + 4, wx + ww - 6, wy + 22);
        if (ch) d.addCircleFilled(wx + ww - 15, wy + 13, 7, 0x44EF4444, 0);
        if (ImGui.isMouseClicked(0) && ch) close();
        d.addText(wx + ww - 20, wy + 6, ch ? 0xFFEF4444 : 0x885B6789, "X");

        ImGui.setCursorPos(8, 30);
        ImGui.beginChild("msgs", ww - 170, wh - 58, true, 0);

        if (spammer.messages.isEmpty()) {
            d.addText(wx + 14, wy + 36, 0x55608199, "Empty. Type a message and press Enter.");
        } else {
            int idx = 0; float y = wy + 34;
            for (String msg : spammer.messages) {
                boolean hover = ImGui.isMouseHoveringRect(wx + 14, y, wx + ww - 178, y + 16);
                if (hover) d.addRectFilled(wx + 14, y, wx + ww - 178, y + 16, 0x221E3A8A, 4, 15);
                d.addText(wx + 18, y + 1, 0xAAA0B0D0, (idx + 1) + ". " + msg);
                if (hover && ImGui.isMouseClicked(1)) { spammer.messages.remove(idx); spammer.saveMessages(); }
                y += 16; idx++;
            }
        }
        ImGui.endChild();

        ImGui.sameLine();
        ImGui.setCursorPos(ww - 156, 30);
        ImGui.beginChild("settings", 148f, wh - 34, false, 0);

        d.addText(ImGui.getCursorScreenPosX(), ImGui.getCursorScreenPosY(), 0xAAFFFFFF, "Delay: " + spammer.cooldownSec + " s");
        ImGui.setCursorPosY(ImGui.getCursorPosY() + 16);
        int[] cds = {spammer.cooldownSec};
        ImGui.pushItemWidth(140);
        if (ImGui.sliderInt("##cd", cds, 1, 60, "%d s")) { spammer.cooldownSec = cds[0]; spammer.saveMessages(); }
        ImGui.popItemWidth();

        ImGui.setCursorPosY(ImGui.getCursorPosY() + 4);
        d.addText(ImGui.getCursorScreenPosX(), ImGui.getCursorScreenPosY(), 0xAAFFFFFF, "AntiSpam:");
        ImGui.setCursorPosY(ImGui.getCursorPosY() + 14);
        float asbx = ImGui.getCursorScreenPosX(), asby = ImGui.getCursorScreenPosY();
        d.addRect(asbx, asby + 2, asbx + 16, asby + 18, spammer.antiSpam ? 0xFF4A6CF7 : 0x553D4570, 4, 15, 2f);
        if (spammer.antiSpam) d.addText(asbx + 3, asby + 3, 0xFFFFFFFF, "P");
        if (ImGui.invisibleButton("##as", 16, 16)) { spammer.antiSpam = !spammer.antiSpam; spammer.saveMessages(); }

        ImGui.setCursorPosY(ImGui.getCursorPosY() + 4);
        d.addText(ImGui.getCursorScreenPosX(), ImGui.getCursorScreenPosY(), 0xAAFFFFFF, "Chars: " + spammer.antiSpamChars);
        ImGui.setCursorPosY(ImGui.getCursorPosY() + 16);
        int[] chc = {spammer.antiSpamChars};
        ImGui.pushItemWidth(140);
        if (ImGui.sliderInt("##chc", chc, 1, 20, "%d")) { spammer.antiSpamChars = chc[0]; spammer.saveMessages(); }
        ImGui.popItemWidth();

        ImGui.setCursorPosY(ImGui.getCursorPosY() + 10);
        d.addText(ImGui.getCursorScreenPosX(), ImGui.getCursorScreenPosY(), 0x55608199, "- Enter = add");
        ImGui.setCursorPosY(ImGui.getCursorPosY() + 12);
        d.addText(ImGui.getCursorScreenPosX(), ImGui.getCursorScreenPosY(), 0x55608199, "- RMB = delete");
        ImGui.setCursorPosY(ImGui.getCursorPosY() + 12);
        d.addText(ImGui.getCursorScreenPosX(), ImGui.getCursorScreenPosY(), 0x55608199, "- Click = edit");

        ImGui.endChild();

        ImGui.setCursorPos(8, wh - 24);
        d.addRectFilled(wx + 8, wy + wh - 24, wx + ww - 8, wy + wh - 6, 0xFF12142A, 4, 15);
        d.addText(wx + 12, wy + wh - 20, 0x555B6789, "Type message... Enter to add");
        ImGui.invisibleButton("##msg", ww - 16, 18);

        ImGui.end();
        ImGui.popStyleColor();
        ImGui.popStyleVar(3);
        ImGuiRenderer.endFrame();
    }
}
