package com.onefizz.mixin;

import com.onefizz.OneFizzMod;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class ReachMixin {

    @Inject(method = "getEntityInteractionRange", at = @At("RETURN"), cancellable = true)
    private void onGetAttackRange(CallbackInfoReturnable<Double> cir) {
        if (OneFizzMod.modules == null) return;
        var reach = OneFizzMod.modules.getReach();
        if (!reach.isEnabled()) return;

        // Apply only to local player
        PlayerEntity self = (PlayerEntity)(Object) this;
        if (!(self instanceof ClientPlayerEntity)) return;
        cir.setReturnValue((double) reach.attackReach);
    }

    @Inject(method = "getBlockInteractionRange", at = @At("RETURN"), cancellable = true)
    private void onGetBlockRange(CallbackInfoReturnable<Double> cir) {
        if (OneFizzMod.modules == null) return;
        var reach = OneFizzMod.modules.getReach();
        if (!reach.isEnabled()) return;

        PlayerEntity self = (PlayerEntity)(Object) this;
        if (!(self instanceof ClientPlayerEntity)) return;
        cir.setReturnValue((double) reach.blockReach);
    }
}
