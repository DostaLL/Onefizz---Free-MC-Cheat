package com.onefizz.mixin;

import com.onefizz.OneFizzMod;
import com.onefizz.modules.Flight;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * GroundSpoofMixin — подменяет onGround=true в исходящих пакетах ТОЛЬКО когда
 * конкретный модуль это запрашивает:
 *   - NoFall (GROUND_SPOOF режим) — только 1 раз за падение через shouldSpoofGround()
 *   - Flight (MATRIX режим) — всегда когда активен
 *   - BoatFly (MATRIX режим) — всегда когда активен
 *
 * НЕ подменяет всегда — это сломало бы другие модули.
 */
@Mixin(PlayerMoveC2SPacket.class)
public abstract class GroundSpoofMixin {

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        try {
            if (OneFizzMod.modules == null) return;
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc == null || mc.player == null) return;

            boolean spoof = false;

            // NoFall GROUND_SPOOF — спрашиваем модуль, нужно ли спуфить ЭТОТ пакет
            var nf = OneFizzMod.modules.getNoFall();
            if (nf != null && nf.shouldSpoofGround()) {
                spoof = true;
            }

            // Flight MATRIX — всегда спуфим onGround когда активен
            var fl = OneFizzMod.modules.getFlight();
            if (fl != null && fl.isEnabled() && fl.mode == Flight.Mode.MATRIX) {
                spoof = true;
            }

            // BoatFly MATRIX — всегда спуфим onGround когда активен
            var bf = OneFizzMod.modules.getBoatFly();
            if (bf != null && bf.isEnabled() && bf.mode == com.onefizz.modules.BoatFly.Mode.MATRIX) {
                spoof = true;
            }

            if (spoof) {
                ((PlayerMoveAccessor)(Object) this).setOnGroundField(true);
            }
        } catch (Throwable ignored) {
        }
    }
}
