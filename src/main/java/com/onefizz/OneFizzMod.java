package com.onefizz;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class OneFizzMod implements ClientModInitializer {

    public static OneFizzMod INSTANCE;
    public static ModuleManager modules;

    private KeyBinding guiKey;

    @Override
    public void onInitializeClient() {
        INSTANCE = this;
        modules = new ModuleManager();
        ConfigManager.init();
        AltManager.INSTANCE.load();
        UserCounter.init();

        guiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.onefizz.menu.v2",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.onefizz"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (guiKey.wasPressed()) {
                if (client.player != null) client.setScreen(new OneFizzScreen());
            }
            if (client.player == null) return;

            modules.getKillAura().onTick(client);
            modules.getAutoClicker().onTick(client);
            modules.getAutoGapple().onTick(client);
            modules.getAutoTotem().onTick(client);
            modules.getBowAimbot().onTick(client);
            modules.getAutoArmor().onTick(client);
            modules.getBacktrack().onTick(client);
            modules.getTargetStrafe().onTick(client);
            modules.getTriggerBot().onTick(client);
            modules.getAutoWeapon().onTick(client);
            modules.getFakeLag().onTick(client);

            modules.getFlight().onTick(client);
            modules.getElytraFly().onTick(client);
            modules.getBoatFly().onTick(client);
            modules.getJesus().onTick(client);
            modules.getSprint().onTick(client);
            modules.getSpeed().onTick(client);
            modules.getScaffold().onTick(client);
            modules.getStrafe().onTick(client);
            modules.getAutoBridge().onTick(client);
            modules.getClickWarp().onTick(client);
            modules.getAirJump().onTick(client);
            modules.getSpider().onTick(client);
            modules.getParkour().onTick(client);
            modules.getNoFall().onTick(client);
            modules.getNoSlowdown().onTick(client);
            modules.getVelocity().onTick(client);

            modules.getAutoEat().onTick(client);
            modules.getFastBreak().onTick(client);
            modules.getAntiVoid().onTick(client);
            modules.getNoHunger().onTick(client);
            modules.getChestStealer().onTick(client);
            modules.getAutoTool().onTick(client);
            modules.getAutoInvite().onTick(client);
            modules.getAntiAFK().onTick(client);
            modules.getAutoLeave().onTick(client);
            modules.getAutoRespawn().onTick(client);
            modules.getSpammer().onTick(client);

            modules.getAutoReg().onTick(client);
            modules.getStaffList().onTick(client);

            modules.getDonkeyDupe().onTick(client);
            modules.getDisabler().onTick(client);
            modules.getAcDetector().onTick(client);
            modules.getTimerBypass().onTick(client);
            modules.getPhase().onTick(client);
        });

        WorldRenderEvents.AFTER_ENTITIES.register(ctx -> {
            modules.getEsp().onRender(ctx);
            modules.getMobESP().onRender(ctx);
            modules.getChestESP().onRender(ctx);
            modules.getJumpCircles().onRender(ctx);
            modules.getChinaHat().onRender(ctx);
            modules.getTrajectories().onRender(ctx);
            modules.getBlockOutline().onRender(ctx);
        });

        HudRenderCallback.EVENT.register((ctx, tickCounter) -> {
            int sw = ctx.getScaledWindowWidth();
            ToastManager.INSTANCE.render(ctx, sw);
            modules.getTargetHUD().onRender(ctx);
            modules.getArrayList().onRender(ctx);
            modules.getPlayerHUD().onRender(ctx);
            modules.getPacketInspector().onRender(ctx);
            modules.getWatermark().onRender(ctx, sw);
        });
    }
}
