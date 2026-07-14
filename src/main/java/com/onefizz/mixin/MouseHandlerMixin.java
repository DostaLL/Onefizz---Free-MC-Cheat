package com.onefizz.mixin;

import com.onefizz.OneFizzMod;
import net.minecraft.client.Mouse;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseHandlerMixin {

    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        if (button != 2 || action != 1) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        if (OneFizzMod.modules == null) return;

        // ClickWarp: телепорт на блок
        if (OneFizzMod.modules.getClickWarp().isEnabled()) {
            if (OneFizzMod.modules.getClickWarp().tryWarp(mc)) {
                ci.cancel();
                return;
            }
        }

        // Friends: добавление/удаление друга по средней кнопке
        HitResult hit = mc.crosshairTarget;
        if (hit == null || hit.getType() != HitResult.Type.ENTITY) return;
        if (!(((EntityHitResult) hit).getEntity() instanceof PlayerEntity target)) return;

        String nick = target.getName().getString();
        OneFizzMod.modules.getKillAura().toggleFriend(nick);

        mc.player.sendMessage(
                net.minecraft.text.Text.literal(
                        OneFizzMod.modules.getKillAura().friends.contains(nick)
                                ? "§a[OneFizz] §f" + nick + " добавлен в друзья"
                                : "§c[OneFizz] §f" + nick + " удалён из друзей"),
                true);
    }
}
