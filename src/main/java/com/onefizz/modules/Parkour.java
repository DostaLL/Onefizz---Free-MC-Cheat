package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class Parkour extends Module {

    public enum Mode { ALWAYS, SPRINT }

    @Setting(name = "Режим") public Mode mode = Mode.ALWAYS;
    @Setting(name = "Тиков до края", min = 1f, max = 5f) public int tickAhead = 2;

    private int edgeTicks = 0;

    public Parkour() { super("Parkour", "Авто-помощник для паркура"); }

    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null || mc.world == null) return;
        if (!mc.player.isOnGround()) { edgeTicks = 0; return; }

        boolean forward = mc.options.forwardKey.isPressed();
        if (!forward) { edgeTicks = 0; return; }

        if (mode == Mode.SPRINT && !mc.player.isSprinting()) { edgeTicks = 0; return; }

        Direction facing = mc.player.getHorizontalFacing();
        BlockPos feetPos = mc.player.getBlockPos();
        BlockPos ahead = feetPos.offset(facing);
        BlockPos aheadDown = ahead.down();
        BlockPos feetDown = feetPos.down();

        boolean solidFeet = mc.world.getBlockState(feetDown).isSolidBlock(mc.world, feetDown);
        boolean solidAhead = mc.world.getBlockState(ahead).isSolidBlock(mc.world, ahead);
        boolean airBelowAhead = mc.world.getBlockState(aheadDown).isAir();

        if (!solidFeet) { edgeTicks = 0; return; }
        if (solidAhead && !airBelowAhead) { edgeTicks = 0; return; }

        edgeTicks++;

        if (edgeTicks >= tickAhead) {
            mc.player.jump();
            edgeTicks = 0;
        }
    }

    @Override
    protected void onDisable() {
        edgeTicks = 0;
    }
}
