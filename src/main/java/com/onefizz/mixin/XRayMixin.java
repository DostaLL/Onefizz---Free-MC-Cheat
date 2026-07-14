package com.onefizz.mixin;

import com.onefizz.OneFizzMod;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkRendererRegion.class)
public class XRayMixin {

    /**
     * При запросе BlockState для рендера чанка:
     * если XRay включён и блок не в списке видимых — возвращаем AIR.
     */
    @Inject(method = "getBlockState", at = @At("RETURN"), cancellable = true)
    private void onGetBlockState(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        if (OneFizzMod.modules == null) return;
        var xray = OneFizzMod.modules.getXRay();
        if (!xray.isEnabled()) return;

        BlockState state = cir.getReturnValue();
        if (state.isAir()) return;
        if (!xray.visibleBlocks.contains(state.getBlock())) {
            cir.setReturnValue(net.minecraft.block.Blocks.AIR.getDefaultState());
        }
    }
}
