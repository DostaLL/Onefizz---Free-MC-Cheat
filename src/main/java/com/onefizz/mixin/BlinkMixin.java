package com.onefizz.mixin;

import com.onefizz.OneFizzMod;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Перехватывает все исходящие пакеты для Blink, FakeLag и PacketLimiter.
 * - Blink: блокирует все PlayerMove пакеты пока активен
 * - FakeLag: буферизирует PlayerMove пакеты и отправляет с задержкой
 * - PacketLimiter: ограничивает количество движенческих пакетов в тик
 */
@Mixin(ClientConnection.class)
public class BlinkMixin {

    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void onSend(Packet<?> packet, CallbackInfo ci) {
        if (OneFizzMod.modules == null) return;

        if (!(packet instanceof PlayerMoveC2SPacket movePacket)) return;

        // Blink: блокируем все movement пакеты
        if (OneFizzMod.modules.getBlink().isBlinking()) {
            ci.cancel();
            return;
        }

        // FakeLag: буферизируем пакет
        if (OneFizzMod.modules.getFakeLag().isEnabled()) {
            OneFizzMod.modules.getFakeLag().tryBuffer(movePacket);
            ci.cancel();
            return;
        }

        // PacketLimiter: ограничиваем packet rate
        if (OneFizzMod.modules.getPacketLimiter().shouldDrop()) {
            ci.cancel();
        }
    }
}
