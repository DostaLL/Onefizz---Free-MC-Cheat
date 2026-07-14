package com.onefizz.mixin;

import com.onefizz.OneFizzMod;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class PacketInspectorMixin {

    @Inject(method = "onEntityVelocityUpdate", at = @At("HEAD"))
    private void onVelocity(EntityVelocityUpdateS2CPacket packet, CallbackInfo ci) {
        try {
            if (OneFizzMod.modules == null) return;
            double vx = packet.getVelocityX() / 8000.0;
            double vy = packet.getVelocityY() / 8000.0;
            double vz = packet.getVelocityZ() / 8000.0;
            OneFizzMod.modules.getPacketInspector().onVelocity(packet.getId(), vx, vy, vz);
            OneFizzMod.modules.getAcDetector().onVelocity(packet.getId(), vx, vy, vz);
        } catch (Throwable ignored) {}
    }

    @Inject(method = "onPlayerPositionLook", at = @At("HEAD"))
    private void onTeleport(PlayerPositionLookS2CPacket packet, CallbackInfo ci) {
        try {
            if (OneFizzMod.modules == null) return;
            OneFizzMod.modules.getPacketInspector().onTeleport(
                packet.getX(), packet.getY(), packet.getZ(),
                packet.getYaw(), packet.getPitch());
            OneFizzMod.modules.getAcDetector().onTeleport(
                packet.getX(), packet.getY(), packet.getZ());
            var mc = net.minecraft.client.MinecraftClient.getInstance();
            if (mc.player != null) {
                double dx = packet.getX() - mc.player.getX();
                double dy = packet.getY() - mc.player.getY();
                double dz = packet.getZ() - mc.player.getZ();
                double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                com.onefizz.AntiLagback.onRubberband(dist);
            }
        } catch (Throwable ignored) {}
    }

    @Inject(method = "onExplosion", at = @At("TAIL"))
    private void onExplosion(ExplosionS2CPacket packet, CallbackInfo ci) {
        try {
            if (OneFizzMod.modules == null) return;
            var mc = net.minecraft.client.MinecraftClient.getInstance();
            if (mc.player == null) return;
            var vel = mc.player.getVelocity();
            OneFizzMod.modules.getPacketInspector().onExplosion(vel.x, vel.y, vel.z);
        } catch (Throwable ignored) {}
    }

}
