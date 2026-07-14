package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.OneFizzMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

import java.util.LinkedList;

public class Backtrack extends Module {

    private final LinkedList<Vec3d> positions = new LinkedList<>();
    private LivingEntity trackedEntity = null;

    public Backtrack() { super("Backtrack", "Ложная задержка позиций врага"); }

    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null) { positions.clear(); return; }

        LivingEntity target = OneFizzMod.modules.getKillAura().getLockedTarget();
        if (target == null || target.isDead()) { positions.clear(); trackedEntity = null; return; }

        if (target != trackedEntity) { positions.clear(); trackedEntity = target; }

        positions.addLast(target.getPos());
        while (positions.size() > 3) positions.removeFirst();
    }

    /** Returns minimum distance from player to any of the stored positions */
    public double getMinDistance(MinecraftClient mc, LivingEntity target) {
        if (!isEnabled() || positions.isEmpty() || target != trackedEntity) {
            return mc.player.distanceTo(target);
        }
        Vec3d playerPos = mc.player.getPos();
        double min = mc.player.distanceTo(target);
        for (Vec3d pos : positions) {
            double d = playerPos.distanceTo(pos);
            if (d < min) min = d;
        }
        return min;
    }
}
