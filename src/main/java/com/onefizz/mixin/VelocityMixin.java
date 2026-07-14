package com.onefizz.mixin;

import com.onefizz.OneFizzMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class VelocityMixin {

    @Inject(method = "onEntityVelocityUpdate", at = @At("HEAD"), cancellable = true)
    private void onVelocityUpdate(EntityVelocityUpdateS2CPacket packet, CallbackInfo ci) {
        if (OneFizzMod.modules == null) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        if (player == null) return;
        if (packet.getId() != player.getId()) return;

        // FakeLag: сброс буфера при получении урона
        OneFizzMod.modules.getFakeLag().onDamage();

        // AntiKnockback — полная отмена
        if (OneFizzMod.modules.getAntiKnockback().isEnabled()) {
            ci.cancel();
            return;
        }
    }

    @Inject(method = "onEntityVelocityUpdate", at = @At("TAIL"))
    private void onVelocityUpdateTail(EntityVelocityUpdateS2CPacket packet, CallbackInfo ci) {
        if (OneFizzMod.modules == null) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        if (player == null) return;
        if (packet.getId() != player.getId()) return;
        if (OneFizzMod.modules.getAntiKnockback().isEnabled()) return;

        var vel = OneFizzMod.modules.getVelocity();
        if (!vel.isEnabled()) return;

        // Не модифицируем сразу — планируем через 1-2 тика (обход Grim velocity-A)
        Vec3d currentVel = player.getVelocity();
        vel.scheduleVelocityModify(currentVel);
    }

    @Inject(method = "onExplosion", at = @At("TAIL"))
    private void onExplosion(ExplosionS2CPacket packet, CallbackInfo ci) {
        if (OneFizzMod.modules == null) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        if (player == null) return;

        boolean akb = OneFizzMod.modules.getAntiKnockback().isEnabled();
        var vel = OneFizzMod.modules.getVelocity();
        boolean velOn = vel.isEnabled() && vel.cancelExplosions;

        if (!akb && !velOn) return;

        if (akb) {
            player.setVelocity(0, 0, 0);
        } else {
            // Для взрывов тоже используем delay
            vel.scheduleVelocityModify(player.getVelocity());
        }
    }
}
