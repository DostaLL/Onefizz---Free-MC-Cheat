package com.onefizz.mixin;

import com.onefizz.OneFizzMod;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleManager.class)
public class NoRenderParticlesMixin {

    @Inject(method = "addParticle(Lnet/minecraft/client/particle/Particle;)V", at = @At("HEAD"), cancellable = true)
    private void cancelParticles(Particle particle, CallbackInfo ci) {
        if (OneFizzMod.modules.getNoRender().isEnabled()
            && OneFizzMod.modules.getNoRender().noParticles) {
            ci.cancel();
        }
    }
}
