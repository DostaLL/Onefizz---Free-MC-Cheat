package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.OneFizzMod;
import com.onefizz.RenderUtils;
import com.onefizz.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public class TargetHUD extends Module {

    @Setting(min = 0f, max = 100f)
    public float xPercent = 50f;

    @Setting(min = 0f, max = 100f)
    public float yPercent = 65f;

    @Setting public boolean showHealthBar = true;
    @Setting public boolean showArmor     = true;
    @Setting public boolean showDistance  = true;

    private float displayedHealth = 20f;

    public TargetHUD() { super("TargetHUD", "Информация о цели KillAura"); }

    public void onRender(DrawContext ctx) {
        if (!isEnabled()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        var ka = OneFizzMod.modules.getKillAura();
        if (!ka.isEnabled()) return;

        // KillAura's lockedTarget is private - use a public proxy via collectTargets if exposed.
        // Simpler approach: find nearest valid target ourselves.
        LivingEntity target = findCurrentTarget(mc, ka);
        if (target == null) return;

        // Smoothed displayed health
        displayedHealth += (target.getHealth() - displayedHealth) * 0.2f;

        int sw = ctx.getScaledWindowWidth();
        int sh = ctx.getScaledWindowHeight();
        int boxW = 130;
        int boxH = showHealthBar ? 44 : 30;
        int x = (int)(sw * xPercent / 100f) - boxW / 2;
        int y = (int)(sh * yPercent / 100f) - boxH / 2;

        // Background panel
        RenderUtils.roundedRect(ctx, x, y, x + boxW, y + boxH, 5, 0xE0080612);
        RenderUtils.roundedOutline(ctx, x, y, x + boxW, y + boxH, 5, 0xFF7C3AED);

        // Name
        String name = target instanceof PlayerEntity p
            ? p.getName().getString()
            : target.getType().getName().getString();
        ctx.drawText(mc.textRenderer, Text.literal(name).styled(s -> s.withBold(true)),
            x + 8, y + 6, 0xFFEDE9FE, false);

        // Distance
        if (showDistance) {
            String dist = String.format("%.1f m", mc.player.distanceTo(target));
            int dw = mc.textRenderer.getWidth(dist);
            ctx.drawText(mc.textRenderer, Text.literal(dist),
                x + boxW - 8 - dw, y + 6, 0xFFA39BB7, false);
        }

        // Armor (icons substitute via armor value text)
        if (showArmor && target instanceof PlayerEntity p) {
            String ar = "❖ " + p.getArmor();
            ctx.drawText(mc.textRenderer, Text.literal(ar),
                x + 8, y + 18, 0xFFC084FC, false);
        }

        // Health bar
        if (showHealthBar) {
            int barX = x + 8, barY = y + boxH - 12, barW = boxW - 16, barH = 6;
            ctx.fill(barX, barY, barX + barW, barY + barH, 0xFF1A1030);
            float maxHp = target.getMaxHealth();
            float pct = Math.max(0f, Math.min(1f, displayedHealth / maxHp));
            int fillW = (int)(barW * pct);
            // Color: red→yellow→green
            float r = pct < 0.5f ? 1f : (1f - (pct - 0.5f) * 2f);
            float g = pct < 0.5f ? pct * 2f : 1f;
            int color = 0xFF000000
                | ((int)(r * 0xFF) << 16)
                | ((int)(g * 0xFF) << 8)
                | 0x20;
            ctx.fill(barX, barY, barX + fillW, barY + barH, color);

            String hp = String.format("%.1f / %.0f", target.getHealth(), maxHp);
            int hw = mc.textRenderer.getWidth(hp);
            ctx.drawText(mc.textRenderer, Text.literal(hp),
                x + (boxW - hw) / 2, barY - 9, 0xFFFFFFFF, false);
        }
    }

    private LivingEntity findCurrentTarget(MinecraftClient mc, KillAura ka) {
        if (mc.world == null) return null;
        return mc.world.getEntitiesByClass(LivingEntity.class,
                mc.player.getBoundingBox().expand(ka.reach + 1.5),
                e -> e != mc.player && !e.isDead() && mc.player.distanceTo(e) <= ka.reach
                  && (e instanceof PlayerEntity ? ka.attackPlayers : ka.attackMobs))
            .stream()
            .min(java.util.Comparator.comparingDouble(e -> mc.player.distanceTo(e)))
            .orElse(null);
    }
}
