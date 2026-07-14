package com.onefizz.mixin;

import com.onefizz.OneFizzMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class HitBoxMixin {

    @Inject(method = "getBoundingBox()Lnet/minecraft/util/math/Box;",
            at = @At("RETURN"), cancellable = true)
    private void onGetBoundingBox(CallbackInfoReturnable<Box> cir) {
        if (OneFizzMod.modules == null) return;
        var hb = OneFizzMod.modules.getHitBox();
        if (!hb.isEnabled()) return;

        Entity self = (Entity)(Object) this;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || self == mc.player) return;

        boolean apply = false;
        if (self instanceof PlayerEntity && hb.affectPlayers) apply = true;
        if (self instanceof MobEntity && hb.affectMobs) apply = true;
        if (!apply) return;

        cir.setReturnValue(cir.getReturnValue().expand(hb.expand));
    }
}
