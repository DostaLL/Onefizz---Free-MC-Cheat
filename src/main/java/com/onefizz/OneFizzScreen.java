package com.onefizz;

import com.onefizz.modules.*;
import imgui.*;
import imgui.flag.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class OneFizzScreen extends Screen {

    private static final int W = 360, H = 260;

    private int gx, gy;
    private static Tab activeTab = Tab.COMBAT;
    private String searchQuery = "";
    private String configInput = "";
    private String configStatus = "";
    private long configStatusTime;
    private String altInput = "";
    private String altStatus = "";
    private long altStatusTime;
    private int altSelected = -1;
    private AltManager.AltType altTypeSelection = AltManager.AltType.OFFLINE;
    private boolean dragging;
    private int dragOffX, dragOffY;

    private enum Tab { COMBAT, VISUALS, MOVEMENT, PLAYER, ALTS, MISC }
    private static final String[] TAB_LABELS = { "Combat", "Visuals", "Movement", "Player", "Alts", "Misc" };

    public OneFizzScreen() { super(Text.literal("OneFizz")); }

    private List<Module> getModulesForTab(Tab tab) {
        ModuleManager m = OneFizzMod.modules;
        return switch (tab) {
            case COMBAT -> List.of(m.getKillAura(), m.getAutoClicker(), m.getCriticals(), m.getAutoGapple(), m.getAutoTotem(), m.getBowAimbot(), m.getReach(), m.getHitBox(), m.getAntiBot(), m.getAutoArmor(), m.getVelocity(), m.getAntiKnockback(), m.getTriggerBot(), m.getAutoWeapon(), m.getFakeLag());
            case VISUALS -> List.of(m.getEsp(), m.getMobESP(), m.getChestESP(), m.getFullbright(), m.getXRay(), m.getJumpCircles(), m.getTargetHUD(), m.getChinaHat(), m.getArrayList(), m.getPlayerHUD(), m.getWatermark(), m.getTrajectories(), m.getNoRender(), m.getBlockOutline(), m.getPerspective());
            case MOVEMENT -> List.of(m.getFlight(), m.getElytraFly(), m.getBoatFly(), m.getSpeed(), m.getSprint(), m.getStrafe(), m.getJesus(), m.getStep(), m.getTimer(), m.getNoFall(), m.getNoSlowdown(), m.getFastPlace(), m.getScaffold(), m.getAutoBridge(), m.getInventoryMove(), m.getClickWarp(), m.getAirJump(), m.getSpider(), m.getParkour());
            case PLAYER -> List.of(m.getAutoEat(), m.getFastBreak(), m.getAntiVoid(), m.getNoHunger(), m.getNoHurt(), m.getChestStealer(), m.getAutoTool(), m.getBlink(), m.getAutoInvite(), m.getPacketInspector(), m.getAntiAFK(), m.getAutoLeave(), m.getAutoRespawn(), m.getDonkeyDupe(), m.getSpammer(), m.getDisabler(), m.getAcDetector(), m.getPacketLimiter(), m.getTimerBypass(), m.getPhase(), m.getAutoReg(), m.getStaffList());
            case MISC, ALTS -> List.of();
        };
    }

    @Override protected void init() { gx = (width - W) / 2; gy = (height - H) / 2; ImGuiRenderer.ensureInitialized(); }
    @Override public boolean shouldPause() { return false; }

    @Override public boolean mouseClicked(double mx, double my, int btn) { ImGuiIO io = ImGui.getIO(); io.setMousePos((float) mx, (float) my); io.setMouseDown(btn, true); int rx = (int) mx - gx, ry = (int) my - gy; if (rx >= 0 && rx < W && ry >= 0 && ry < 26 && btn == 0) { dragging = true; dragOffX = rx; dragOffY = ry; return true; } return io.getWantCaptureMouse() || super.mouseClicked(mx, my, btn); }
    @Override public boolean mouseReleased(double mx, double my, int btn) { dragging = false; ImGui.getIO().setMousePos((float) mx, (float) my); ImGui.getIO().setMouseDown(btn, false); return super.mouseReleased(mx, my, btn); }
    @Override public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) { if (dragging) { gx = (int) mx - dragOffX; gy = (int) my - dragOffY; return true; } ImGui.getIO().setMousePos((float) mx, (float) my); return super.mouseDragged(mx, my, btn, dx, dy); }
    @Override public boolean mouseScrolled(double mx, double my, double h, double v) { ImGuiIO io = ImGui.getIO(); io.setMouseWheelH(io.getMouseWheelH() + (float) h); io.setMouseWheel(io.getMouseWheel() + (float) v); return io.getWantCaptureMouse() || super.mouseScrolled(mx, my, h, v); }
    @Override public boolean keyPressed(int k, int s, int mods) { ImGuiIO io = ImGui.getIO(); io.setKeysDown(k, true); io.setKeyCtrl((mods & GLFW.GLFW_MOD_CONTROL) != 0); io.setKeyShift((mods & GLFW.GLFW_MOD_SHIFT) != 0); io.setKeyAlt((mods & GLFW.GLFW_MOD_ALT) != 0); if (io.getWantCaptureKeyboard()) return true; if (k == GLFW.GLFW_KEY_ESCAPE) { close(); return true; } return super.keyPressed(k, s, mods); }
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

        int f = ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoBringToFrontOnFocus | ImGuiWindowFlags.NoNavFocus | ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse;
        ImGui.begin("OneFizz", f);

        ImDrawList d = ImGui.getWindowDrawList();
        float wx = ImGui.getWindowPosX(), wy = ImGui.getWindowPosY(), ww = ImGui.getWindowWidth(), wh = ImGui.getWindowHeight();

        d.addRectFilled(wx, wy, wx + ww, wy + wh, 0xFF0B0C1E, 10, 15);
        d.addRectFilled(wx + 6, wy, wx + ww - 6, wy + 26, 0xFF0F1030, 10, 3);
        d.addLine(wx + 10, wy + 26, wx + ww - 10, wy + 26, 0xFF2563EB, 2f);

        long ct = OneFizzMod.modules.getAll().stream().filter(Module::isEnabled).count();
        long tt = OneFizzMod.modules.getAll().size();
        String cnt = ct + "/" + tt;
        d.addText(wx + 14, wy + 6, 0xFFFFFFFF, "ONEFIZZ");
        d.addText(wx + 16 + ImGui.calcTextSize("ONEFIZZ").x, wy + 7, 0x66608199, "by IMaska");
        d.addText(wx + ww - ImGui.calcTextSize(cnt).x - 34, wy + 6, 0xFF60A5FA, cnt);

        boolean ch = ImGui.isMouseHoveringRect(wx + ww - 24, wy + 4, wx + ww - 6, wy + 22);
        if (ch) d.addCircleFilled(wx + ww - 15, wy + 13, 7, 0x44EF4444, 0);
        d.addText(wx + ww - 20, wy + 6, ch ? 0xFFEF4444 : 0x885B6789, "X");
        if (ImGui.isMouseClicked(0) && ch) close();

        ImGui.setCursorPos(4, 28);
        ImGui.beginChild("sidebar", 80f, wh - 32, false, 0);

        Tab[] tabs = Tab.values();
        for (int i = 0; i < tabs.length; i++) {
            boolean a = tabs[i] == activeTab;
            int bg = a ? 0xFF1E3A8A : 0;
            float ry = ImGui.getCursorScreenPosY();
            d.addRectFilled(wx + 4, ry, wx + 84, ry + 24, bg, 6, 15);
            if (ImGui.isMouseHoveringRect(wx + 4, ry, wx + 84, ry + 24) && !a) d.addRectFilled(wx + 4, ry, wx + 84, ry + 24, 0x331E3A8A, 6, 15);
            if (ImGui.invisibleButton(TAB_LABELS[i], 80, 24)) { activeTab = tabs[i]; searchQuery = ""; }
            d.addText(wx + 16, ry + 4, a ? 0xFFFFFFFF : 0x99608199, TAB_LABELS[i]);
        }
        ImGui.endChild();

        ImGui.sameLine();
        ImGui.setCursorPos(88, 28);
        float cw2 = ImGui.getContentRegionAvailX();
        ImGui.beginChild("content", cw2, wh - 32, false, 0);

        if (activeTab == Tab.MISC) renderMiscPanel();
        else if (activeTab == Tab.ALTS) renderAltsPanel();
        else renderModuleList();

        ImGui.endChild();
        ImGui.end();
        ImGui.popStyleColor();
        ImGui.popStyleVar(3);
        ImGuiRenderer.endFrame();
    }

    private void renderModuleList() {
        ImDrawList d = ImGui.getWindowDrawList();
        float cx = ImGui.getCursorScreenPosX(), cy = ImGui.getCursorScreenPosY(), cw = ImGui.getContentRegionAvailX();

        d.addRectFilled(cx, cy, cx + cw, cy + 18, 0xFF12142A, 5, 15);
        ImGui.setCursorPos(4, ImGui.getCursorPosY() + 2);
        d.addText(cx + 8, cy + 2, searchQuery.isEmpty() ? 0x555B6789 : 0xAAFFFFFF, searchQuery.isEmpty() ? "Search..." : searchQuery);
        ImGui.invisibleButton("##search", cw - 8, 16);

        ImGui.setCursorPos(4, ImGui.getCursorPosY() + 2);
        ImGui.beginChild("mods", cw, ImGui.getContentRegionAvailY(), false, 0);

        List<Module> mods = getModulesForTab(activeTab);
        if (!searchQuery.isEmpty()) { String q = searchQuery.toLowerCase(); mods = mods.stream().filter(m -> m.getName().toLowerCase().contains(q)).toList(); }

        for (int i = 0; i < mods.size(); i++) {
            Module mod = mods.get(i);
            boolean en = mod.isEnabled();
            float rx = ImGui.getCursorScreenPosX(), ry = ImGui.getCursorScreenPosY(), rw = cw - 6, rh = 22;
            boolean hover = ImGui.isMouseHoveringRect(rx, ry, rx + rw, ry + rh);
            int bg = en ? 0x331E3A8A : 0x18121E3A;
            if (hover) bg = en ? 0x443A5CF6 : 0x281E3A8A;
            d.addRectFilled(rx, ry, rx + rw, ry + rh, bg, 5, 15);
            if (en) d.addRectFilled(rx + 2, ry + 3, rx + 5, ry + rh - 3, 0xFF4A6CF7, 2, 15);

            d.addCircle(rx + rw - 14, ry + rh / 2f, 6, en ? 0xFF4A6CF7 : 0x553D4570, 0, 1f);
            if (en) d.addCircleFilled(rx + rw - 14, ry + rh / 2f, 3, 0xFFFFFFFF, 0);

            if (ImGui.isMouseClicked(0) && hover) mod.toggle();
            if (ImGui.isMouseClicked(1) && hover) { if (mod instanceof Spammer sp) client.setScreen(new SpammerSettingsScreen(this, sp)); else client.setScreen(new ModuleSettingsScreen(this, mod)); }

            d.addText(rx + 10, ry + 4, en ? 0xFFE0E0F0 : 0x99A0A0CC, mod.getName());
            if (mod.getKeyBind() != -1) { String kb = GLFW.glfwGetKeyName(mod.getKeyBind(), 0); if (kb == null) kb = String.valueOf(mod.getKeyBind()); d.addText(rx + 10 + ImGui.calcTextSize(mod.getName()).x + 4, ry + 4, 0x555B6789, "[" + kb.toUpperCase() + "]"); }
            ImGui.setCursorPosY(ry + rh + 1);
        }
        ImGui.endChild();
    }

    private void renderMiscPanel() {
        ImDrawList d = ImGui.getWindowDrawList();
        float cw = ImGui.getContentRegionAvailX();

        ImGui.setCursorPos(4, 4);
        d.addText(ImGui.getCursorScreenPosX(), ImGui.getCursorScreenPosY(), 0x998899CC, "Theme:");
        ImGui.sameLine(); ImGui.setCursorPos(ImGui.getCursorPosX() + 8, ImGui.getCursorPosY());
        Theme[] th = Theme.values(); Theme cur = Theme.get();
        if (ImGui.button(cur.label() + "  < >", cw - ImGui.getCursorPosX() - 2, 18)) Theme.set(th[(cur.ordinal() + 1) % th.length]);

        ImGui.setCursorPos(4, ImGui.getCursorPosY() + 6);
        d.addText(ImGui.getCursorScreenPosX(), ImGui.getCursorScreenPosY(), 0xFFFFFFFF, "Configs");

        ImGui.setCursorPos(4, ImGui.getCursorPosY() + 18);
        d.addRectFilled(ImGui.getCursorScreenPosX() + 4, ImGui.getCursorScreenPosY(), ImGui.getCursorScreenPosX() + cw - 4, ImGui.getCursorScreenPosY() + 18, 0xFF12142A, 4, 15);
        d.addText(ImGui.getCursorScreenPosX() + 8, ImGui.getCursorScreenPosY() + 3, configInput.isEmpty() ? 0x555B6789 : 0xAAFFFFFF, configInput.isEmpty() ? "config name..." : configInput);
        ImGui.invisibleButton("##cfg", cw - 10, 18);

        ImGui.setCursorPos(4, ImGui.getCursorPosY() + 4);
        float bw = (cw - 24) / 3f;
        if (ImGui.button("Save", bw, 18)) doSaveConfig();
        ImGui.sameLine(); if (ImGui.button("Load", bw, 18)) doLoadConfig();
        ImGui.sameLine(); if (ImGui.button("Delete", bw, 18)) doDeleteConfig();

        if (!configStatus.isEmpty() && System.currentTimeMillis() - configStatusTime < 3000) { ImGui.setCursorPos(4, ImGui.getCursorPosY() + 4); d.addText(ImGui.getCursorScreenPosX(), ImGui.getCursorScreenPosY(), 0x88889ACC, configStatus); }
        ImGui.setCursorPos(4, ImGui.getCursorPosY() + 8);
        d.addText(ImGui.getCursorScreenPosX(), ImGui.getCursorScreenPosY(), 0x55608199, "Saved:");
        List<String> cfgs = ConfigManager.listConfigs();
        if (cfgs.isEmpty()) { ImGui.setCursorPos(8, ImGui.getCursorPosY() + 14); d.addText(ImGui.getCursorScreenPosX(), ImGui.getCursorScreenPosY(), 0x445B6789, "(none)"); }
        else { for (int i = 0; i < cfgs.size() && i < 5; i++) { ImGui.setCursorPos(8, ImGui.getCursorPosY() + 2); boolean s = cfgs.get(i).equals(configInput); if (ImGui.invisibleButton(cfgs.get(i), cw - 20, 14)) configInput = cfgs.get(i); d.addText(ImGui.getCursorScreenPosX() - ImGui.getCursorPosX() + 4, ImGui.getCursorScreenPosY() + 1, s ? 0xFFCCDDFF : 0x88608199, cfgs.get(i)); } }
    }

    private void renderAltsPanel() {
        ImDrawList d = ImGui.getWindowDrawList();
        float cw = ImGui.getContentRegionAvailX();

        ImGui.setCursorPos(4, 2);
        String curUsr = "current: " + MinecraftClient.getInstance().getSession().getUsername();
        d.addText(ImGui.getCursorScreenPosX(), ImGui.getCursorScreenPosY(), 0xFFFFFFFF, "Alt Manager");
        d.addText(ImGui.getCursorScreenPosX() + cw - ImGui.calcTextSize(curUsr).x - 4, ImGui.getCursorScreenPosY(), 0x88608199, curUsr);

        ImGui.setCursorPos(4, ImGui.getCursorPosY() + 16);
        d.addText(ImGui.getCursorScreenPosX(), ImGui.getCursorScreenPosY(), 0x88608199, "Type:");
        ImGui.sameLine(); String tl = altTypeSelection == AltManager.AltType.OFFLINE ? "Offline" : "Microsoft";
        if (ImGui.button(tl + "  < >", cw - ImGui.getCursorPosX() - 2, 16)) altTypeSelection = altTypeSelection == AltManager.AltType.OFFLINE ? AltManager.AltType.MICROSOFT : AltManager.AltType.OFFLINE;

        ImGui.setCursorPos(4, ImGui.getCursorPosY() + 6);
        d.addRectFilled(ImGui.getCursorScreenPosX() + 4, ImGui.getCursorScreenPosY(), ImGui.getCursorScreenPosX() + cw - 4, ImGui.getCursorScreenPosY() + 18, 0xFF12142A, 4, 15);
        d.addText(ImGui.getCursorScreenPosX() + 8, ImGui.getCursorScreenPosY() + 3, altInput.isEmpty() ? 0x555B6789 : 0xAAFFFFFF, altInput.isEmpty() ? "username..." : altInput);
        ImGui.invisibleButton("##alt", cw - 10, 18);

        ImGui.setCursorPos(4, ImGui.getCursorPosY() + 4);
        float bw = (cw - 24) / 3f;
        if (ImGui.button("Add", bw, 18)) doAddAlt(); ImGui.sameLine(); if (ImGui.button("Login", bw, 18)) doSwitchAlt(); ImGui.sameLine(); if (ImGui.button("Remove", bw, 18)) doRemoveAlt();
        if (altTypeSelection == AltManager.AltType.MICROSOFT) { ImGui.setCursorPos(4, ImGui.getCursorPosY() + 4); if (ImGui.button("Microsoft Login (Browser)", cw - 10, 18)) doMicrosoftLogin(); }

        int ny = (int)ImGui.getCursorPosY() + 10;
        if (!altStatus.isEmpty() && System.currentTimeMillis() - altStatusTime < 5000) { ImGui.setCursorPos(4, ny); d.addText(ImGui.getCursorScreenPosX(), ImGui.getCursorScreenPosY(), 0x88889ACC, altStatus); ny += 14; }
        ImGui.setCursorPos(4, ny); d.addText(ImGui.getCursorScreenPosX(), ImGui.getCursorScreenPosY(), 0x55608199, "Saved alts:"); ny += 14;
        var alts = AltManager.INSTANCE.getAlts();
        if (alts.isEmpty()) { ImGui.setCursorPos(8, ny); d.addText(ImGui.getCursorScreenPosX(), ImGui.getCursorScreenPosY(), 0x445B6789, "(none)"); }
        else { for (int i = 0; i < alts.size() && i < 5; i++) { var a = alts.get(i); String nl = a.username + (a.type == AltManager.AltType.MICROSOFT ? " [MS]" : ""); ImGui.setCursorPos(8, ny + i * 18); if (ImGui.invisibleButton(nl, cw - 20, 16)) { altSelected = i; altInput = a.username; altTypeSelection = a.type; } d.addText(ImGui.getCursorScreenPosX() - ImGui.getCursorPosX() + 4, ImGui.getCursorScreenPosY() + 1, i == altSelected ? 0xFFCCDDFF : 0x88608199, nl); } }
    }

    private void doSaveConfig() { if (!configInput.isBlank()) { String n = configInput.trim(); ConfigManager.save(n); configStatus = "Saved: " + n; configStatusTime = System.currentTimeMillis(); } }
    private void doLoadConfig() { if (!configInput.isBlank()) { String n = configInput.trim(); ConfigManager.load(n); configStatus = "Loaded: " + n; configStatusTime = System.currentTimeMillis(); } }
    private void doDeleteConfig() { if (!configInput.isBlank()) { String n = configInput.trim(); ConfigManager.delete(n); configStatus = "Deleted: " + n; configStatusTime = System.currentTimeMillis(); configInput = ""; } }
    private void doAddAlt() { if (altInput.isBlank()) return; AltManager.Alt a = new AltManager.Alt(altInput.trim(), altTypeSelection, ""); AltManager.INSTANCE.add(a); altStatus = "Added: " + altInput.trim(); altStatusTime = System.currentTimeMillis(); altInput = ""; }
    private void doSwitchAlt() { if (altSelected < 0) { altStatus = "Select an alt first"; altStatusTime = System.currentTimeMillis(); return; } var al = AltManager.INSTANCE.getAlts(); if (altSelected >= al.size()) return; AltManager.INSTANCE.switchTo(al.get(altSelected)); altStatus = "Switched"; altStatusTime = System.currentTimeMillis(); }
    private void doRemoveAlt() { if (altSelected < 0) return; AltManager.INSTANCE.remove(altSelected); altStatus = "Removed"; altStatusTime = System.currentTimeMillis(); altSelected = -1; altInput = ""; }
    private void doMicrosoftLogin() { altStatus = "Requesting MS code..."; altStatusTime = System.currentTimeMillis(); MicrosoftAuth.startDeviceCode().thenAccept(dc -> { altStatus = "Code: " + dc.userCode + " | " + dc.verificationUri; altStatusTime = System.currentTimeMillis(); try { java.awt.Desktop.getDesktop().browse(new java.net.URI(dc.verificationUri)); } catch (Exception x) {} MicrosoftAuth.pollToken(dc).thenAccept(res -> { AltManager.Alt a = new AltManager.Alt(res.username, AltManager.AltType.MICROSOFT, ""); a.uuid = res.uuid.toString(); a.accessToken = res.accessToken; a.refreshToken = res.refreshToken; AltManager.INSTANCE.add(a); altStatus = "OK: " + res.username; altStatusTime = System.currentTimeMillis(); }).exceptionally(x -> { altStatus = "Failed: " + x.getMessage(); altStatusTime = System.currentTimeMillis(); return null; }); }).exceptionally(x -> { altStatus = "Failed: " + x.getMessage(); altStatusTime = System.currentTimeMillis(); return null; }); }
}
