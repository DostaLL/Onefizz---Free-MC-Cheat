package com.onefizz;

import imgui.*;
import imgui.flag.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;

public class ModuleSettingsScreen extends Screen {

    private static final int W = 290, H = 250;
    private final Screen parent;
    private final Module module;
    private int gx, gy;
    private boolean dragging, awaitingBind;
    private int dragOffX, dragOffY;

    public ModuleSettingsScreen(Screen parent, Module module) { super(Text.literal("OneFizz - " + module.getName())); this.parent = parent; this.module = module; }
    @Override protected void init() { gx = (width - W) / 2; gy = (height - H) / 2; ImGuiRenderer.ensureInitialized(); }
    @Override public boolean shouldPause() { return false; }
    @Override public void close() { client.setScreen(parent); }

    @Override public boolean mouseClicked(double mx, double my, int btn) { ImGuiIO io = ImGui.getIO(); io.setMousePos((float) mx, (float) my); io.setMouseDown(btn, true); int rx = (int) mx - gx, ry = (int) my - gy; if (rx >= 0 && rx < W && ry >= 0 && ry < 26 && btn == 0) { dragging = true; dragOffX = rx; dragOffY = ry; return true; } return io.getWantCaptureMouse() || super.mouseClicked(mx, my, btn); }
    @Override public boolean mouseReleased(double mx, double my, int btn) { dragging = false; ImGui.getIO().setMousePos((float) mx, (float) my); ImGui.getIO().setMouseDown(btn, false); return super.mouseReleased(mx, my, btn); }
    @Override public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) { if (dragging) { gx = (int) mx - dragOffX; gy = (int) my - dragOffY; return true; } ImGui.getIO().setMousePos((float) mx, (float) my); return super.mouseDragged(mx, my, btn, dx, dy); }
    @Override public boolean mouseScrolled(double mx, double my, double h, double v) { ImGuiIO io = ImGui.getIO(); io.setMouseWheelH(io.getMouseWheelH() + (float) h); io.setMouseWheel(io.getMouseWheel() + (float) v); return io.getWantCaptureMouse() || super.mouseScrolled(mx, my, h, v); }
    @Override public boolean keyPressed(int k, int s, int mods) { ImGuiIO io = ImGui.getIO(); io.setKeysDown(k, true); io.setKeyCtrl((mods & GLFW.GLFW_MOD_CONTROL) != 0); io.setKeyShift((mods & GLFW.GLFW_MOD_SHIFT) != 0); io.setKeyAlt((mods & GLFW.GLFW_MOD_ALT) != 0); if (io.getWantCaptureKeyboard()) return true; if (awaitingBind) { if (k == GLFW.GLFW_KEY_ESCAPE) module.setKeyBind(-1); else module.setKeyBind(k); awaitingBind = false; return true; } if (k == GLFW.GLFW_KEY_ESCAPE) { close(); return true; } return super.keyPressed(k, s, mods); }
    @Override public boolean keyReleased(int k, int s, int mods) { ImGui.getIO().setKeysDown(k, false); return ImGui.getIO().getWantCaptureKeyboard() || super.keyReleased(k, s, mods); }
    @Override public boolean charTyped(char c, int mods) { ImGui.getIO().addInputCharacter(c); return ImGui.getIO().getWantCaptureKeyboard() || super.charTyped(c, mods); }

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
        ImGui.begin("Settings##" + module.getName(), fl);

        ImDrawList d = ImGui.getWindowDrawList();
        float wx = ImGui.getWindowPosX(), wy = ImGui.getWindowPosY(), ww = ImGui.getWindowWidth(), wh = ImGui.getWindowHeight();

        d.addRectFilled(wx, wy, wx + ww, wy + wh, 0xFF0B0C1E, 10, 15);
        d.addRectFilled(wx + 6, wy, wx + ww - 6, wy + 26, 0xFF0F1030, 10, 3);
        d.addLine(wx + 10, wy + 26, wx + ww - 10, wy + 26, 0xFF2563EB, 2f);
        d.addText(wx + 14, wy + 6, 0xFFFFFFFF, module.getName());

        boolean ch = ImGui.isMouseHoveringRect(wx + ww - 24, wy + 4, wx + ww - 6, wy + 22);
        if (ch) d.addCircleFilled(wx + ww - 15, wy + 13, 7, 0x44EF4444, 0);
        if (ImGui.isMouseClicked(0) && ch) close();
        d.addText(wx + ww - 20, wy + 6, ch ? 0xFFEF4444 : 0x885B6789, "X");

        ImGui.setCursorPos(8, 30);
        ImGui.beginChild("scroll", ww - 16, wh - 34, false, 0);

        d.addText(ImGui.getCursorScreenPosX(), ImGui.getCursorScreenPosY(), 0x88608199, module.getDescription());
        ImGui.setCursorPosY(ImGui.getCursorPosY() + 14);

        String bl = awaitingBind ? "Press a key... (Esc = clear)" : "Bind: " + keyName(module.getKeyBind());
        if (ImGui.button(bl, ww - 32, 22)) awaitingBind = !awaitingBind;
        ImGui.setCursorPosY(ImGui.getCursorPosY() + 6);

        boolean en = module.isEnabled();
        float bx = ImGui.getCursorScreenPosX(), by = ImGui.getCursorScreenPosY();
        d.addRectFilled(bx, by, bx + ww - 32, by + 24, en ? 0xFF1E3A8A : 0xFF12142A, 5, 15);
        d.addText(bx + (ww - 32 - ImGui.calcTextSize(en ? "ENABLED" : "DISABLED").x) / 2f, by + 5, 0xFFCCDDFF, en ? "ENABLED" : "DISABLED");
        if (ImGui.invisibleButton("##toggle", ww - 32, 24)) module.toggle();
        ImGui.setCursorPosY(ImGui.getCursorPosY() + 2);

        ImGui.setCursorPosY(ImGui.getCursorPosY() + 6);
        d.addLine(ImGui.getCursorScreenPosX(), ImGui.getCursorScreenPosY(), ImGui.getCursorScreenPosX() + ww - 32, ImGui.getCursorScreenPosY(), 0x332563EB);
        ImGui.setCursorPosY(ImGui.getCursorPosY() + 6);

        for (Field fld : module.getSettings()) {
            if (module instanceof com.onefizz.modules.KillAura ka && !ka.isSettingVisible(fld.getName())) continue;
            Setting ann = fld.getAnnotation(Setting.class);
            String label = ann.name().isEmpty() ? fld.getName() : ann.name();
            Class<?> type = fld.getType();
            try {
                if (type == boolean.class || type == Boolean.class) {
                    boolean val = (boolean) fld.get(module);
                    float sx = ImGui.getCursorScreenPosX(), sy = ImGui.getCursorScreenPosY();
                    d.addRect(sx, sy + 2, sx + 16, sy + 18, val ? 0xFF4A6CF7 : 0x553D4570, 4, 15, 2f);
                    if (val) d.addText(sx + 3, sy + 3, 0xFFFFFFFF, "P");
                    d.addText(sx + 22, sy + 3, val ? 0xAAFFFFFF : 0x88608199, label);
                    if (ImGui.invisibleButton("##cb" + fld.getName().hashCode(), ww - 40, 18)) { fld.set(module, !val); if (module instanceof com.onefizz.modules.XRay x) { x.rebuildSet(); MinecraftClient.getInstance().worldRenderer.reload(); } }
                } else if (type.isEnum()) {
                    Enum<?> val = (Enum<?>) fld.get(module);
                    float sx = ImGui.getCursorScreenPosX(), sy = ImGui.getCursorScreenPosY();
                    d.addText(sx, sy + 3, 0x88608199, label);
                    String vs = val.name();
                    d.addRectFilled(ImGui.getCursorScreenPosX() + ww - ImGui.calcTextSize(vs).x - 40, sy, ImGui.getCursorScreenPosX() + ww - 40, sy + 18, 0xFF1A1E42, 4, 15);
                    d.addText(ImGui.getCursorScreenPosX() + ww - ImGui.calcTextSize(vs).x - 38, sy + 3, 0xFF60A5FA, vs);
                    if (ImGui.invisibleButton("##enum" + fld.getName().hashCode(), ww - 40, 18)) { Object[] vls = type.getEnumConstants(); Enum<?> next = (Enum<?>) vls[(val.ordinal() + 1) % vls.length]; fld.set(module, next); if (module instanceof com.onefizz.modules.KillAura ka) ka.onSettingChanged(fld.getName(), next); }
                } else if (type == String.class) {
                    String sv = (String) fld.get(module);
                    float sx = ImGui.getCursorScreenPosX(), sy = ImGui.getCursorScreenPosY();
                    d.addText(sx, sy + 3, 0x88608199, label + ":");
                    d.addRectFilled(sx + ImGui.calcTextSize(label + ":").x + 8, sy, sx + ww - 40, sy + 18, 0xFF12142A, 4, 15);
                    d.addText(sx + ImGui.calcTextSize(label + ":").x + 12, sy + 3, 0xAAFFFFFF, sv);
                    if (ImGui.invisibleButton("##str" + fld.getName().hashCode(), ww - 40, 18)) {}
                } else if (isNumeric(type)) {
                    float[] v = {((Number) fld.get(module)).floatValue()};
                    float min = getMin(fld), max = min + getRange(fld);
                    ImGui.pushItemWidth(ww - 34);
                    if (ImGui.sliderFloat(label, v, min, max, "%.2f")) fld.set(module, castType(fld, v[0]));
                    ImGui.popItemWidth();
                }
            } catch (IllegalAccessException ig) {}
            ImGui.setCursorPosY(ImGui.getCursorPosY() + 3);
        }

        ImGui.endChild();
        ImGui.end();
        ImGui.popStyleColor();
        ImGui.popStyleVar(3);
        ImGuiRenderer.endFrame();
    }

    private boolean isNumeric(Class<?> t) { return t == float.class || t == Float.class || t == int.class || t == Integer.class || t == double.class || t == Double.class; }
    private float getMin(Field f) { Setting s = f.getAnnotation(Setting.class); return (s != null && s.min() != Float.MIN_VALUE) ? s.min() : 0f; }
    private float getRange(Field f) { float min = getMin(f); Setting s = f.getAnnotation(Setting.class); return ((s != null && s.max() != Float.MAX_VALUE) ? s.max() : 10f) - min; }
    private Object castType(Field f, float val) { Class<?> t = f.getType(); if (t == int.class || t == Integer.class) return (int) val; if (t == double.class || t == Double.class) return (double) val; return val; }

    private String keyName(int code) { if (code <= 0) return "NONE"; try { String n = GLFW.glfwGetKeyName(code, 0); if (n != null && !n.isEmpty()) return n.toUpperCase(); } catch (Throwable ig) {} return switch (code) { case GLFW.GLFW_KEY_LEFT_SHIFT -> "LSHIFT"; case GLFW.GLFW_KEY_RIGHT_SHIFT -> "RSHIFT"; case GLFW.GLFW_KEY_LEFT_CONTROL -> "LCTRL"; case GLFW.GLFW_KEY_RIGHT_CONTROL -> "RCTRL"; case GLFW.GLFW_KEY_LEFT_ALT -> "LALT"; case GLFW.GLFW_KEY_RIGHT_ALT -> "RALT"; case GLFW.GLFW_KEY_TAB -> "TAB"; case GLFW.GLFW_KEY_SPACE -> "SPACE"; case GLFW.GLFW_KEY_ENTER -> "ENTER"; case GLFW.GLFW_KEY_ESCAPE -> "ESC"; case GLFW.GLFW_KEY_BACKSPACE -> "BACKSPACE"; case GLFW.GLFW_KEY_DELETE -> "DEL"; default -> "KEY " + code; }; }
}
