package com.onefizz.modules;

import com.onefizz.AnimeMascot;
import com.onefizz.Module;
import com.onefizz.RenderUtils;
import com.onefizz.Setting;
import com.onefizz.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.text.Text;

/**
 * PlayerHUD — компактная панель статов игрока в углу экрана.
 *
 * Скорость семплируется раз в 250мс и плавно tween'ится — больше не скачет.
 */
public class PlayerHUD extends Module {

    public enum AvatarMode { PLAYER_SKIN, ANIME_GIRL, NONE }

    private static final int C_BG       = 0xCC0E0A1A;
    private static final int C_OUTLINE  = 0xFF2E1530;
    private static final int C_TEXT     = 0xFFEDE9FE;
    private static final int C_TEXT_DIM = 0xFFA39BB7;
    private static final int C_HP       = 0xFFFF7095;
    private static final int C_ARMOR    = 0xFFB5C7FF;
    private static final int C_HUNGER   = 0xFFFCD34D;
    private static final int C_SPEED    = 0xFFC084FC;

    @Setting(name = "X позиция", min = 0f, max = 500f) public int posX = 6;
    @Setting(name = "Y позиция", min = 0f, max = 500f) public int posY = 6;
    @Setting(name = "Аватар") public AvatarMode avatarMode = AvatarMode.PLAYER_SKIN;
    @Setting(name = "Показывать ник") public boolean showName = true;
    @Setting(name = "Показывать HP") public boolean showHealth = true;
    @Setting(name = "Показывать броню") public boolean showArmor = true;
    @Setting(name = "Показывать голод") public boolean showHunger = true;
    @Setting(name = "Показывать скорость") public boolean showSpeed = true;
    @Setting(name = "Прозрачность фона", min = 0.3f, max = 1f) public float bgAlpha = 0.85f;

    // Speed tracking — семплинг + tween
    private double sampleStartX = Double.NaN, sampleStartZ = Double.NaN;
    private long sampleStartMs = 0;
    private float measuredSpeed = 0f;   // последнее измеренное значение
    private float displayedSpeed = 0f;  // плавно интерполированное значение
    private long lastFrameMs = 0;

    public PlayerHUD() {
        super("PlayerHUD", "Информация о персонаже на экране");
        setEnabledSilent(true);
    }

    public void onRender(DrawContext ctx) {
        if (!isEnabled()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        ClientPlayerEntity p = mc.player;

        // ── Speed: sample раз в 250мс, потом tween ─────────────────────────────
        long nowMs = System.currentTimeMillis();
        if (Double.isNaN(sampleStartX)) {
            sampleStartX = p.getX();
            sampleStartZ = p.getZ();
            sampleStartMs = nowMs;
        } else if (nowMs - sampleStartMs >= 250) {
            double dx = p.getX() - sampleStartX;
            double dz = p.getZ() - sampleStartZ;
            double dt = (nowMs - sampleStartMs) / 1000.0;
            measuredSpeed = (float) (Math.sqrt(dx * dx + dz * dz) / dt);
            sampleStartX = p.getX();
            sampleStartZ = p.getZ();
            sampleStartMs = nowMs;
        }
        // Tween от displayed к measured
        long frameDelta = lastFrameMs == 0 ? 16 : Math.min(100, nowMs - lastFrameMs);
        lastFrameMs = nowMs;
        float t = Math.min(1f, frameDelta / 250f); // 250мс на полное сближение
        displayedSpeed = displayedSpeed + (measuredSpeed - displayedSpeed) * t;
        // Snap к 0 если совсем близко
        if (Math.abs(displayedSpeed) < 0.02f) displayedSpeed = 0f;

        // ── Layout ─────────────────────────────────────────────────────────────
        int x = posX;
        int y = posY;
        int width = 130;

        boolean useAvatar = avatarMode != AvatarMode.NONE;
        int avatarSize = (avatarMode == AvatarMode.ANIME_GIRL) ? 28 : 22;

        int statRows = 0;
        if (showHealth) statRows++;
        if (showArmor) statRows++;
        if (showHunger) statRows++;
        if (showSpeed) statRows++;

        int headerH = useAvatar ? avatarSize + 6 : (showName ? 14 : 0);
        int statsH = statRows * 11;
        int height = 6 + headerH + statsH + 6;

        // ── Glow ───────────────────────────────────────────────────────────────
        for (int i = 4; i > 0; i--) {
            RenderUtils.roundedOutline(ctx, x - i, y - i, x + width + i, y + height + i,
                7 + i, RenderUtils.withAlpha(Theme.GLOW(), 0.18f * i / 4f));
        }

        // ── Background gradient (тёмно-розовый сверху → почти чёрный снизу) ───
        int bgTop = (int) (((C_BG >>> 24) & 0xFF) * bgAlpha) << 24 | 0x1A0F2E;
        int bgBot = (int) (((C_BG >>> 24) & 0xFF) * bgAlpha) << 24 | 0x0A0614;

        // Сначала закрашиваем округлый фон базовым цветом
        RenderUtils.roundedRect(ctx, x, y, x + width, y + height, 7,
            (int) (((C_BG >>> 24) & 0xFF) * bgAlpha) << 24 | (C_BG & 0x00FFFFFF));

        // Outline + accent slash слева
        RenderUtils.roundedOutline(ctx, x, y, x + width, y + height, 7, C_OUTLINE);
        ctx.fill(x + 2, y + 4, x + 4, y + height - 4, Theme.A());

        // ── Header (avatar + name) ─────────────────────────────────────────────
        int contentX = x + 7;
        int textStartX = contentX;
        int headerY = y + 4;

        if (useAvatar) {
            if (avatarMode == AvatarMode.PLAYER_SKIN) {
                try {
                    SkinTextures skin = p.getSkinTextures();
                    PlayerSkinDrawer.draw(ctx, skin, contentX, headerY, avatarSize);
                    // Розовая рамка вокруг скина
                    RenderUtils.roundedOutline(ctx,
                        contentX - 1, headerY - 1,
                        contentX + avatarSize + 1, headerY + avatarSize + 1,
                        2, Theme.A());
                } catch (Throwable ignored) {}
            } else if (avatarMode == AvatarMode.ANIME_GIRL) {
                float hpPct = p.getHealth() / Math.max(1f, p.getMaxHealth());
                AnimeMascot.Mood mood = hpPct > 0.5f ? AnimeMascot.Mood.HAPPY
                                      : (hpPct < 0.3f ? AnimeMascot.Mood.HURT
                                                       : AnimeMascot.Mood.NORMAL);
                boolean blink = (nowMs / 100) % 40 < 2;
                AnimeMascot.draw(ctx, contentX, headerY, 1, mood, blink);
            }
            textStartX = contentX + avatarSize + 6;
        }

        if (showName) {
            String name = p.getName().getString();
            if (name.length() > 12) name = name.substring(0, 12);
            ctx.drawText(mc.textRenderer, Text.literal(name).styled(s -> s.withBold(true)),
                textStartX, headerY + 2, C_TEXT, true);
            // Под ником — тонкая надпись
            ctx.drawText(mc.textRenderer, Text.literal("OneFizz"),
                textStartX, headerY + 12, RenderUtils.withAlpha(Theme.ALT(), 0.7f), false);
        }

        // ── Stats ──────────────────────────────────────────────────────────────
        int statsY = y + 6 + headerH;
        int statW = width - 12;

        if (showHealth) {
            float hp = p.getHealth();
            float maxHp = p.getMaxHealth();
            drawStatRow(ctx, mc, x + 6, statsY, statW,
                "HP", String.format("%.0f / %.0f", hp, maxHp),
                hp / Math.max(1f, maxHp), C_HP);
            statsY += 11;
        }
        if (showArmor) {
            int armor = p.getArmor();
            drawStatRow(ctx, mc, x + 6, statsY, statW,
                "Armor", String.valueOf(armor), armor / 20f, C_ARMOR);
            statsY += 11;
        }
        if (showHunger) {
            int food = p.getHungerManager().getFoodLevel();
            drawStatRow(ctx, mc, x + 6, statsY, statW,
                "Food", String.valueOf(food), food / 20f, C_HUNGER);
            statsY += 11;
        }
        if (showSpeed) {
            drawStatRow(ctx, mc, x + 6, statsY, statW,
                "Speed", String.format("%.2f b/s", displayedSpeed),
                Math.min(1f, displayedSpeed / 10f), C_SPEED);
        }
    }

    private void drawStatRow(DrawContext ctx, MinecraftClient mc, int x, int y, int width,
                              String label, String value, float pct, int color) {
        pct = Math.max(0f, Math.min(1f, pct));

        // Фон строки (тонкая полоска под текстом)
        ctx.fill(x, y + 9, x + width, y + 10, 0x44000000);

        // Текст
        ctx.drawText(mc.textRenderer, Text.literal(label), x, y + 1, C_TEXT_DIM, false);
        int valueWidth = mc.textRenderer.getWidth(value);
        ctx.drawText(mc.textRenderer, Text.literal(value),
            x + width - valueWidth, y + 1, C_TEXT, false);

        // Прогресс-бар (тонкий, под текстом)
        int filledW = (int) (width * pct);
        if (filledW > 0) {
            // Glow за баром
            ctx.fill(x, y + 9, x + filledW, y + 10, color);
            // Тонкий хайлайт сверху
            ctx.fill(x, y + 9, x + filledW, y + 9 + 1,
                RenderUtils.withAlpha(color, 1.0f));
            // Glow tip на конце
            if (filledW < width && filledW > 1) {
                ctx.fill(x + filledW - 1, y + 8, x + filledW + 1, y + 11,
                    RenderUtils.withAlpha(color, 0.6f));
            }
        }
    }
}
