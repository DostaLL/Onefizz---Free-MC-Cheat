package com.onefizz;

import imgui.*;
import imgui.flag.*;
import imgui.gl3.ImGuiImplGl3;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

public final class ImGuiRenderer {

    private static boolean initialized;
    private static ImGuiImplGl3 gl3;

    public static void ensureInitialized() {
        if (initialized) return;
        initialized = true;

        ImGui.createContext();
        ImGuiIO io = ImGui.getIO();
        io.setIniFilename(null);

        ImFontAtlas fonts = io.getFonts();
        try {
            ImFontConfig cfg = new ImFontConfig();
            cfg.setOversampleH(5);
            cfg.setOversampleV(5);
            cfg.setPixelSnapH(false);
            fonts.addFontFromFileTTF("C:\\Windows\\Fonts\\segoeui.ttf", 17, cfg, fonts.getGlyphRangesCyrillic());
            cfg.destroy();
        } catch (Exception ignored) {
            ImFontConfig cfg = new ImFontConfig();
            cfg.setOversampleH(5);
            cfg.setOversampleV(5);
            cfg.setSizePixels(17f);
            fonts.addFontDefault(cfg);
            cfg.destroy();
        }

        gl3 = new ImGuiImplGl3();
        gl3.init("#version 150");

        applyBlueStyle();
    }

    private static void applyBlueStyle() {
        ImGui.styleColorsDark();
        ImGuiStyle s = ImGui.getStyle();
        s.setWindowRounding(10f);
        s.setChildRounding(8f);
        s.setFrameRounding(6f);
        s.setGrabRounding(6f);
        s.setScrollbarRounding(6f);
        s.setTabRounding(6f);
        s.setWindowBorderSize(0f);
        s.setWindowPadding(10, 10);
        s.setFramePadding(8, 5);
        s.setItemSpacing(8, 5);
        s.setScrollbarSize(6f);
        s.setGrabMinSize(10f);

        sc(ImGuiCol.WindowBg, 0xFF0C0D20);
        sc(ImGuiCol.ChildBg, 0xEE121532);
        sc(ImGuiCol.PopupBg, 0xEE1A1B38);
        sc(ImGuiCol.Border, 0xFF202568);
        sc(ImGuiCol.FrameBg, 0x8012142A);
        sc(ImGuiCol.FrameBgHovered, 0xA01A1E42);
        sc(ImGuiCol.FrameBgActive, 0xCC23276A);
        sc(ImGuiCol.TitleBg, 0xFF0B0C1E);
        sc(ImGuiCol.TitleBgActive, 0xFF0F1030);
        sc(ImGuiCol.ScrollbarGrab, 0xCC2D3189);
        sc(ImGuiCol.ScrollbarGrabHovered, 0xCC4448A8);
        sc(ImGuiCol.ScrollbarGrabActive, 0xCC5559CC);
        sc(ImGuiCol.CheckMark, 0xFF4A6CF7);
        sc(ImGuiCol.SliderGrab, 0xFF4A6CF7);
        sc(ImGuiCol.SliderGrabActive, 0xFF6B8AFF);
        sc(ImGuiCol.Button, 0xFF1E2468);
        sc(ImGuiCol.ButtonHovered, 0xFF2D3591);
        sc(ImGuiCol.ButtonActive, 0xFF3D46B0);
        sc(ImGuiCol.Header, 0xFF1E2468);
        sc(ImGuiCol.HeaderHovered, 0xFF2D3591);
        sc(ImGuiCol.HeaderActive, 0xFF3D46B0);
        sc(ImGuiCol.Separator, 0xFF2D3189);
        sc(ImGuiCol.SeparatorHovered, 0xFF5559CC);
        sc(ImGuiCol.SeparatorActive, 0xFF4A6CF7);
        sc(ImGuiCol.Tab, 0xFF12142A);
        sc(ImGuiCol.TabHovered, 0xFF2D3591);
        sc(ImGuiCol.TabActive, 0xFF3D46B0);
    }

    private static void sc(int idx, int hex) {
        ImGui.getStyle().setColor(idx,
            ((hex >> 16) & 0xFF) / 255f,
            ((hex >> 8) & 0xFF) / 255f,
            (hex & 0xFF) / 255f,
            ((hex >> 24) & 0xFF) / 255f);
    }

    public static void beginFrame(float deltaTime) {
        if (!initialized) ensureInitialized();
        ImGuiIO io = ImGui.getIO();
        MinecraftClient mc = MinecraftClient.getInstance();
        io.setDisplaySize(mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight());
        io.setDisplayFramebufferScale(1f, 1f);
        io.setDeltaTime(Math.max(0.001f, deltaTime));
        ImGui.newFrame();
    }

    public static void endFrame() {
        ImGui.render();
        ImDrawData dd = ImGui.getDrawData();
        if (dd == null) return;

        int fbW = (int)(dd.getDisplaySizeX() * dd.getFramebufferScaleX());
        int fbH = (int)(dd.getDisplaySizeY() * dd.getFramebufferScaleY());
        if (fbW <= 0 || fbH <= 0) return;

        int prevProg = GL20.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        int prevTex = GL20.glGetInteger(GL_TEXTURE_BINDING_2D);
        int prevVao = GL30.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
        int prevArr = glGetInteger(GL_ARRAY_BUFFER_BINDING);
        int prevElm = glGetInteger(GL_ELEMENT_ARRAY_BUFFER_BINDING);
        boolean prevBlend = GL20.glIsEnabled(GL_BLEND);
        boolean prevCull = GL20.glIsEnabled(GL_CULL_FACE);
        boolean prevDepth = GL20.glIsEnabled(GL_DEPTH_TEST);
        boolean prevScissor = GL20.glIsEnabled(GL_SCISSOR_TEST);

        gl3.renderDrawData(dd);

        GL20.glUseProgram(prevProg);
        GL20.glBindTexture(GL_TEXTURE_2D, prevTex);
        GL30.glBindVertexArray(prevVao);
        glBindBuffer(GL_ARRAY_BUFFER, prevArr);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, prevElm);
        if (prevBlend) GL20.glEnable(GL_BLEND); else GL20.glDisable(GL_BLEND);
        if (prevCull) GL20.glEnable(GL_CULL_FACE); else GL20.glDisable(GL_CULL_FACE);
        if (prevDepth) GL20.glEnable(GL_DEPTH_TEST); else GL20.glDisable(GL_DEPTH_TEST);
        if (prevScissor) GL20.glEnable(GL_SCISSOR_TEST); else GL20.glDisable(GL_SCISSOR_TEST);
    }

    public static void dispose() {
        if (!initialized) return;
        gl3.dispose();
        ImGui.destroyContext();
        initialized = false;
    }
}
