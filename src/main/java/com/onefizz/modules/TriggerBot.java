package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.OneFizzMod;
import com.onefizz.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;

public class TriggerBot extends Module {

    @Setting(name = "Задержка (мс)", min = 0f, max = 500f)
    public int delay = 0;

    @Setting(name = "Бить игроков")
    public boolean attackPlayers = true;

    @Setting(name = "Бить мобов")
    public boolean attackMobs = true;

    private long lastAttack = 0;

    public TriggerBot() { super("TriggerBot", "Автоудар при наведении на врага"); }

    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null || mc.interactionManager == null) return;
        if (!(mc.crosshairTarget instanceof EntityHitResult ehr)) return;
        if (!(ehr.getEntity() instanceof LivingEntity target)) return;
        if (target instanceof PlayerEntity p) {
            if (!attackPlayers) return;
            if (OneFizzMod.modules.getKillAura().friends.contains(p.getName().getString())) return;
        } else if (target instanceof MobEntity) {
            if (!attackMobs) return;
        }
        if (mc.player.getAttackCooldownProgress(0.5f) < 0.9f) return;
        long now = System.currentTimeMillis();
        if (now - lastAttack < delay) return;
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        lastAttack = now;
    }
}
