package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.Setting;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BlockItem;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class Scaffold extends Module {

    public enum Mode { NORMAL, GODBRIDGE, MOONWALK, TOWER }

    @Setting(name = "Режим") public Mode mode = Mode.NORMAL;
    @Setting(name = "Задержка (тики)", min = 0f, max = 5f) public int delay = 0;

    private int tickCounter = 0;
    private int godBridgeCounter = 0;

    public Scaffold() { super("Scaffold", "Автостроительство мостов и башен"); }

    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!(mc.player.getInventory().getMainHandStack().getItem() instanceof BlockItem)) return;

        tickCounter++;
        if (tickCounter <= delay) return;
        tickCounter = 0;

        switch (mode) {
            case NORMAL -> tickNormal(mc);
            case GODBRIDGE -> tickGodBridge(mc);
            case MOONWALK -> tickMoonWalk(mc);
            case TOWER -> tickTower(mc);
        }
    }

    private void tickNormal(MinecraftClient mc) {
        var player = mc.player;
        BlockPos under = BlockPos.ofFloored(player.getX(), player.getBoundingBox().minY - 0.01, player.getZ());

        if (mc.world.getBlockState(under).isReplaceable()) {
            placeAt(mc, under);
            return;
        }

        Direction moveDir = getMovementDirection(player);
        if (moveDir != null) {
            BlockPos forward = under.offset(moveDir);
            if (mc.world.getBlockState(forward).isReplaceable()) {
                double edgeDist = getEdgeDistance(player, moveDir);
                if (edgeDist < 0.55) {
                    placeAt(mc, forward);
                }
            }
        }
    }

    private void tickGodBridge(MinecraftClient mc) {
        var player = mc.player;
        mc.options.backKey.setPressed(true);
        mc.options.rightKey.setPressed(true);
        mc.options.sneakKey.setPressed(true);

        if (placeUnder(mc)) {
            godBridgeCounter++;
        }

        if (godBridgeCounter >= 6) {
            if (player.isOnGround()) {
                player.jump();
                godBridgeCounter = 0;
            }
        }
    }

    private void tickMoonWalk(MinecraftClient mc) {
        mc.options.backKey.setPressed(true);
        mc.options.leftKey.setPressed(true);
        placeUnder(mc);
    }

    private void tickTower(MinecraftClient mc) {
        var player = mc.player;
        BlockPos at = player.getBlockPos();
        if (placeAt(mc, at)) {
            if (mc.options.jumpKey.isPressed()) {
                if (player.isOnGround()) {
                    player.jump();
                } else if (player.getVelocity().y < 0.1) {
                    player.setVelocity(player.getVelocity().x, 0.42, player.getVelocity().z);
                }
            }
        }
    }

    private boolean placeUnder(MinecraftClient mc) {
        var player = mc.player;
        BlockPos under = BlockPos.ofFloored(player.getX(), player.getBoundingBox().minY - 0.01, player.getZ());
        return placeAt(mc, under);
    }

    private boolean placeAt(MinecraftClient mc, BlockPos target) {
        var player = mc.player;
        BlockState state = mc.world.getBlockState(target);
        if (!state.isReplaceable()) return false;

        Neighbour n = findNeighbour(mc, target);
        if (n == null) return false;

        Vec3d hitVec = Vec3d.ofCenter(n.againstPos).add(
            n.face.getOffsetX() * 0.5, n.face.getOffsetY() * 0.5, n.face.getOffsetZ() * 0.5);
        float[] rot = calcRot(player.getEyePos(), hitVec);

        player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
            rot[0], MathHelper.clamp(rot[1], -90f, 90f), player.isOnGround()));

        BlockHitResult hit = new BlockHitResult(hitVec, n.face, n.againstPos, false);
        var result = mc.interactionManager.interactBlock(player, Hand.MAIN_HAND, hit);
        if (result.isAccepted()) player.swingHand(Hand.MAIN_HAND);
        return result.isAccepted();
    }

    private Direction getMovementDirection(net.minecraft.entity.player.PlayerEntity player) {
        Vec3d vel = player.getVelocity();
        if (Math.abs(vel.x) < 0.03 && Math.abs(vel.z) < 0.03) return null;
        if (Math.abs(vel.x) > Math.abs(vel.z)) {
            return vel.x > 0 ? Direction.EAST : Direction.WEST;
        } else {
            return vel.z > 0 ? Direction.SOUTH : Direction.NORTH;
        }
    }

    private double getEdgeDistance(net.minecraft.entity.player.PlayerEntity player, Direction dir) {
        double fx = player.getX() - Math.floor(player.getX());
        double fz = player.getZ() - Math.floor(player.getZ());
        return switch (dir) {
            case NORTH -> fz;
            case SOUTH -> 1.0 - fz;
            case WEST -> fx;
            case EAST -> 1.0 - fx;
            default -> 0.5;
        };
    }

    private record Neighbour(BlockPos againstPos, Direction face) {}

    private Neighbour findNeighbour(MinecraftClient mc, BlockPos target) {
        Direction[] order = { Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP };
        for (Direction d : order) {
            BlockPos against = target.offset(d);
            BlockState st = mc.world.getBlockState(against);
            if (st.isAir() || st.isReplaceable()) continue;
            if (!st.isSolid()) continue;
            return new Neighbour(against, d.getOpposite());
        }
        return null;
    }

    private float[] calcRot(Vec3d from, Vec3d to) {
        Vec3d dir = to.subtract(from).normalize();
        float yaw = (float) Math.toDegrees(Math.atan2(-dir.x, dir.z));
        float pitch = (float) Math.toDegrees(-Math.asin(dir.y));
        return new float[]{ yaw, MathHelper.clamp(pitch, -90f, 90f) };
    }

    @Override
    protected void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null && mc.options != null) {
            mc.options.sneakKey.setPressed(false);
            mc.options.backKey.setPressed(false);
            mc.options.leftKey.setPressed(false);
            mc.options.rightKey.setPressed(false);
        }
        godBridgeCounter = 0;
    }
}
