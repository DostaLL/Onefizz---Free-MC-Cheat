package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.OneFizzMod;
import com.onefizz.RotationManager;
import com.onefizz.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.*;

/**
 * KillAura v5 — простая, понятная, с анти-детектом.
 *
 * Режимы (переключаются через "Версия PvP"):
 * - LEGACY (1.8.9): спам-атака, частые удары, без cooldown.
 * - MODERN (1.12.2+): учитывает attack-cooldown 1.9+, криты в прыжке.
 *
 * Анти-детект:
 * - Плавная наводка с микро-дрожанием (только вблизи цели).
 * - Случайная задержка между ударами ±20%.
 * - Иногда промахивается (естественность).
 * - Не бьет идеально в центр хитбокса.
 * - Скорость наводки ограничена — без резких рывков.
 */
public class KillAura extends Module {

    public enum PvPVersion {
        LEGACY("1.8.9"),
        MODERN("1.12.2+");
        public final String label;
        PvPVersion(String label) { this.label = label; }
    }

    public enum AimMode {
        INSTANT("Мгновенно"),
        SMOOTH("Плавно"),
        SILENT("Скрытно");
        public final String label;
        AimMode(String label) { this.label = label; }
    }

    public enum Priority {
        DISTANCE("Ближайший"),
        ANGLE("По углу"),
        HEALTH("Меньше HP");
        public final String label;
        Priority(String label) { this.label = label; }
    }

    // === Видимые настройки (простые и понятные) ===

    @Setting(name = "Версия PvP")
    public PvPVersion pvpVersion = PvPVersion.LEGACY;

    @Setting(name = "Режим наводки")
    public AimMode aimMode = AimMode.SMOOTH;

    @Setting(name = "Приоритет цели")
    public Priority priority = Priority.ANGLE;

    @Setting(name = "Дальность", min = 2.5f, max = 4.5f)
    public float reach = 3.2f;

    @Setting(name = "Скорость наводки", min = 0.2f, max = 1.0f)
    public float aimSpeed = 0.55f;

    @Setting(name = "Удары в сек", min = 6f, max = 16f)
    public int aps = 10;

    @Setting(name = "FOV", min = 45f, max = 180f)
    public float fov = 120f;

    @Setting(name = "Бить игроков")
    public boolean attackPlayers = true;

    @Setting(name = "Бить мобов")
    public boolean attackMobs = false;

    @Setting(name = "Фильтр ботов")
    public boolean filterBots = true;

    @Setting(name = "Проверка видимости")
    public boolean raytrace = true;

    @Setting(name = "Мульти-цель")
    public boolean multiTarget = false;

    @Setting(name = "Анти-детект")
    public boolean antiDetect = true;

    @Setting(name = "Криты в прыжке")
    public boolean jumpCrits = false;

    @Setting(name = "Экономить удары",
             description = "Не бить, пока цель неуязвима после предыдущего удара")
    public boolean saveHits = false;

    @Setting(name = "Auto W-Tap")
    public boolean wTap = false;

    // === Скрытые поля (не в настройках) ===

    public final Set<String> friends = new HashSet<>();

    private long lastAttackTime = 0;
    private long nextAttackDelay = 0;
    private final Random random = new Random();

    private LivingEntity lockedTarget = null;
    private int targetStickTicks = 0;

    private int kills = 0, hits = 0, combo = 0;
    private long lastHitTime = 0;
    private int wTapTimer = 0;

    // Prediction smoothing
    private Vec3d smoothedVel = Vec3d.ZERO;

    // Preset tracking
    private PvPVersion appliedPreset = null;

    public KillAura() {
        super("KillAura", "Автоатака с 1.8 и 1.12 режимами");
    }

    /** Применяет пресет ТОЛЬКО когда пользователь кликает на "Версия PvP" в UI. */
    public void onSettingChanged(String fieldName, Object value) {
        if (!"pvpVersion".equals(fieldName)) return;
        if (pvpVersion == appliedPreset) return;
        appliedPreset = pvpVersion;

        switch (pvpVersion) {
            case LEGACY -> {
                reach = 3.2f;
                aps = 10;
                aimSpeed = 0.6f;
                aimMode = AimMode.SMOOTH;
                wTap = false;
                saveHits = false;   // по умолчанию спамим, не ждем iframe
                jumpCrits = false;
                raytrace = true;
                fov = 120f;
                multiTarget = false;
                filterBots = true;
                attackPlayers = true;
                attackMobs = false;
                antiDetect = true;
            }
            case MODERN -> {
                reach = 3.0f;
                aps = 10; // не используется, но пусть будет
                aimSpeed = 0.45f;
                aimMode = AimMode.SMOOTH;
                wTap = false;
                saveHits = false;
                jumpCrits = false;
                raytrace = true;
                fov = 120f;
                multiTarget = false;
                filterBots = true;
                attackPlayers = true;
                attackMobs = false;
                antiDetect = true;
            }
        }
    }

    /** Возвращает true, если настройка видима для текущей версии PvP. */
    public boolean isSettingVisible(String fieldName) {
        if (pvpVersion == PvPVersion.LEGACY) {
            // В Legacy скрываем Modern-only настройки
            return !"jumpCrits".equals(fieldName);
        } else {
            // В Modern скрываем Legacy-only настройки
            return !"aps".equals(fieldName) && !"saveHits".equals(fieldName);
        }
    }

    public void onTick(MinecraftClient mc) {
        try { tickInner(mc); }
        catch (Throwable t) { t.printStackTrace(); }
    }

    private void tickInner(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null || mc.world == null) return;
        if (mc.interactionManager == null) return;

        // W-Tap
        if (wTapTimer > 0) {
            wTapTimer--;
        } else if (wTap && mc.options.forwardKey.isPressed()) {
            mc.options.forwardKey.setPressed(false);
            wTapTimer = 2;
        }

        // Combo decay
        if (combo > 0 && System.currentTimeMillis() - lastHitTime > 3000) {
            combo = 0;
        }

        // Collect targets
        List<LivingEntity> targets = collectTargets(mc);
        if (targets.isEmpty()) {
            lockedTarget = null;
            targetStickTicks = 0;
            return;
        }

        // Select target with sticky lock
        LivingEntity primary = selectTarget(mc, targets);
        if (primary == null) { lockedTarget = null; return; }
        lockedTarget = primary;

        // Aim at target
        Vec3d aimPoint = computeAimPoint(mc, primary);
        float[] targetRot = lookAt(mc.player.getEyePos(), aimPoint);
        applyRotation(mc, targetRot[0], targetRot[1]);

        // AutoWeapon
        OneFizzMod.modules.getAutoWeapon().trySwitch(mc);

        // === Attack timing ===
        long now = System.nanoTime();
        if (now - lastAttackTime < nextAttackDelay) return;

        // Modern: attack cooldown
        if (pvpVersion == PvPVersion.MODERN) {
            if (mc.player.getAttackCooldownProgress(0.5f) < 0.93f) return;
        }

        // Modern: jump crits
        if (pvpVersion == PvPVersion.MODERN && jumpCrits) {
            if (mc.player.isOnGround() || mc.player.fallDistance <= 0.05f) {
                lastAttackTime = now;
                nextAttackDelay = computeNextDelay();
                return;
            }
        }

        // Legacy: saveHits = не бить во время iframe (больше урона, меньше CPS)
        if (pvpVersion == PvPVersion.LEGACY && saveHits && primary.hurtTime > 0) {
            lastAttackTime = now;
            nextAttackDelay = computeNextDelay();
            return;
        }

        // Anti-detect: occasional miss
        if (antiDetect && random.nextFloat() < 0.03f) {
            lastAttackTime = now;
            nextAttackDelay = computeNextDelay();
            return;
        }

        // Visibility check
        if (raytrace && !canSeeTarget(mc, primary)) {
            lastAttackTime = now;
            nextAttackDelay = computeNextDelay();
            return;
        }

        // Attack primary
        OneFizzMod.modules.getCriticals().prepareHit(mc);
        attackEntity(mc, primary);
        hits++;
        combo++;
        lastHitTime = System.currentTimeMillis();
        if (primary.getHealth() <= 0) kills++;

        // Multi-target
        if (multiTarget) {
            for (LivingEntity t : targets) {
                if (t == primary) continue;
                if (mc.player.distanceTo(t) > reach) continue;
                if (pvpVersion == PvPVersion.LEGACY && saveHits && t.hurtTime > 0) continue;
                if (raytrace && !canSeeTarget(mc, t)) continue;
                attackEntity(mc, t);
                hits++;
            }
        }

        lastAttackTime = now;
        nextAttackDelay = computeNextDelay();
    }

    // ── Target Selection ─────────────────────────────────────────────────────

    private LivingEntity selectTarget(MinecraftClient mc, List<LivingEntity> targets) {
        LivingEntity best = targets.get(0);

        if (lockedTarget == null || lockedTarget.isDead() || !targets.contains(lockedTarget)) {
            targetStickTicks = 0;
            return best;
        }

        // Hard lock: don't switch while target alive and in range
        if (mc.player.distanceTo(lockedTarget) <= reach + 0.5) {
            return lockedTarget;
        }

        // Target moved out of range — check if we should switch
        if (best != lockedTarget) {
            targetStickTicks++;
            if (targetStickTicks > 5) {
                targetStickTicks = 0;
                return best;
            }
        } else {
            targetStickTicks = 0;
        }

        return lockedTarget;
    }

    // ── Aim ──────────────────────────────────────────────────────────────────

    private Vec3d computeAimPoint(MinecraftClient mc, LivingEntity target) {
        Vec3d eyes = mc.player.getEyePos();
        Box box = target.getBoundingBox();

        // Smoothed prediction
        Vec3d vel = target.getVelocity();
        smoothedVel = new Vec3d(
            smoothedVel.x * 0.5 + vel.x * 0.5,
            smoothedVel.y * 0.5 + vel.y * 0.5,
            smoothedVel.z * 0.5 + vel.z * 0.5
        );
        box = box.offset(smoothedVel.x * 0.1, smoothedVel.y * 0.1, smoothedVel.z * 0.1);

        // Nearest point with micro offset (not always center)
        Vec3d nearest = new Vec3d(
            MathHelper.clamp(eyes.x, box.minX, box.maxX),
            MathHelper.clamp(eyes.y, box.minY + 0.3, box.maxY - 0.3),
            MathHelper.clamp(eyes.z, box.minZ, box.maxZ)
        );

        // Anti-detect: slight random offset so it's not pixel-perfect
        if (antiDetect) {
            nearest = nearest.add(
                (random.nextFloat() - 0.5f) * 0.08,
                (random.nextFloat() - 0.5f) * 0.08,
                (random.nextFloat() - 0.5f) * 0.08
            );
        }

        return nearest;
    }

    private void applyRotation(MinecraftClient mc, float targetYaw, float targetPitch) {
        float curYaw = mc.player.getYaw();
        float curPitch = mc.player.getPitch();

        if (aimMode == AimMode.SILENT) {
            curYaw = RotationManager.isActive() ? RotationManager.getYaw() : curYaw;
            curPitch = RotationManager.isActive() ? RotationManager.getPitch() : curPitch;
        }

        float diffYaw = MathHelper.wrapDegrees(targetYaw - curYaw);
        float diffPitch = targetPitch - curPitch;

        // Deadzone — stop micro-adjusting when close enough
        if (Math.abs(diffYaw) < 0.8f && Math.abs(diffPitch) < 0.5f) {
            if (aimMode == AimMode.SILENT) RotationManager.setOverride(curYaw, curPitch);
            return;
        }

        float finalYaw, finalPitch;

        switch (aimMode) {
            case INSTANT -> {
                finalYaw = targetYaw;
                finalPitch = targetPitch;
            }
            case SMOOTH -> {
                // Speed varies with distance — faster when far, slower when close
                float dist = (float) Math.sqrt(diffYaw * diffYaw + diffPitch * diffPitch);
                float t = Math.min(1f, dist / 45f);
                float ease = t * t * (3f - 2f * t); // smoothstep
                float speed = aimSpeed * (0.2f + 0.8f * ease);

                // Max degrees per tick — prevents snapping
                float maxStep = 5f + 4f * aimSpeed;
                float yawStep = MathHelper.clamp(diffYaw * speed, -maxStep, maxStep);
                float pitchStep = MathHelper.clamp(diffPitch * speed, -maxStep, maxStep);

                finalYaw = curYaw + yawStep;
                finalPitch = curPitch + pitchStep;

                // Micro jitter only when very close (human imperfection)
                if (antiDetect && dist < 3f) {
                    finalYaw += (random.nextFloat() - 0.5f) * 0.08f;
                    finalPitch += (random.nextFloat() - 0.5f) * 0.05f;
                }
            }
            case SILENT -> {
                finalYaw = targetYaw;
                finalPitch = targetPitch;
            }
            default -> {
                finalYaw = targetYaw;
                finalPitch = targetPitch;
            }
        }

        // Apply GCD (mouse sensitivity snap)
        finalYaw = applyGCD(finalYaw, mc);
        finalPitch = applyGCD(finalPitch, mc);
        finalPitch = MathHelper.clamp(finalPitch, -90f, 90f);

        if (aimMode == AimMode.SILENT) {
            RotationManager.setOverride(finalYaw, finalPitch);
        } else {
            mc.player.setYaw(MathHelper.wrapDegrees(finalYaw));
            mc.player.setPitch(finalPitch);
        }
    }

    // ── Timing ───────────────────────────────────────────────────────────────

    private long computeNextDelay() {
        if (pvpVersion == PvPVersion.MODERN) {
            // Modern: just check cooldown every tick, small random delay
            return 45_000_000L + (long)(random.nextFloat() * 20_000_000L);
        }
        // Legacy: CPS-based with ±25% randomization
        long base = 1_000_000_000L / Math.max(1, aps);
        long jitter = (long)((random.nextFloat() - 0.5f) * base * 0.5f);
        return Math.max(35_000_000L, base + jitter);
    }

    private float applyGCD(float angle, MinecraftClient mc) {
        double sens = mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2;
        float gcd = (float)(sens * sens * sens * 1.2);
        if (gcd <= 0) return angle;
        return angle - (angle % gcd);
    }

    // ── Visibility ───────────────────────────────────────────────────────────

    private boolean canSeeTarget(MinecraftClient mc, LivingEntity target) {
        Vec3d eyes = mc.player.getEyePos();
        Vec3d aim = computeAimPoint(mc, target);
        if (raytraceCheck(mc, eyes, aim)) return true;
        // Fallback: center of hitbox
        return raytraceCheck(mc, eyes, target.getBoundingBox().getCenter());
    }

    private boolean raytraceCheck(MinecraftClient mc, Vec3d from, Vec3d to) {
        var hit = mc.world.raycast(new RaycastContext(
            from, to,
            RaycastContext.ShapeType.COLLIDER,
            RaycastContext.FluidHandling.NONE,
            mc.player));
        return hit.getType() != net.minecraft.util.hit.HitResult.Type.BLOCK;
    }

    // ── Target Collection ────────────────────────────────────────────────────

    private List<LivingEntity> collectTargets(MinecraftClient mc) {
        AntiBot antiBot = OneFizzMod.modules.getAntiBot();
        boolean useAntiBot = filterBots && antiBot.isEnabled();
        Backtrack bt = OneFizzMod.modules.getBacktrack();

        return mc.world.getEntitiesByClass(LivingEntity.class,
                mc.player.getBoundingBox().expand(reach + 2.0),
                e -> {
                    if (e == mc.player || e.isDead()) return false;
                    if (bt.getMinDistance(mc, e) > reach) return false;
                    if (fov < 360f && angleDelta(mc, e) > fov / 2f) return false;
                    if (e instanceof PlayerEntity p) {
                        if (!attackPlayers) return false;
                        if (friends.contains(p.getName().getString())) return false;
                        if (useAntiBot && antiBot.isBot(p)) return false;
                        // Team detect via name color
                        if (isSameTeam(p, mc.player)) return false;
                    } else {
                        if (!attackMobs) return false;
                    }
                    return true;
                })
            .stream()
            .sorted(byPriority(mc))
            .toList();
    }

    private Comparator<LivingEntity> byPriority(MinecraftClient mc) {
        return switch (priority) {
            case DISTANCE -> Comparator.comparingDouble(e -> mc.player.distanceTo(e));
            case HEALTH -> Comparator.comparingDouble(LivingEntity::getHealth);
            case ANGLE -> Comparator.comparingDouble(e -> angleDelta(mc, e));
        };
    }

    private double angleDelta(MinecraftClient mc, LivingEntity e) {
        Vec3d eyes = mc.player.getEyePos();
        Vec3d dir = e.getEyePos().subtract(eyes).normalize();
        float yaw = (float) Math.toDegrees(Math.atan2(-dir.x, dir.z));
        return Math.abs(MathHelper.wrapDegrees(mc.player.getYaw() - yaw));
    }

    private boolean isSameTeam(PlayerEntity a, PlayerEntity b) {
        String ca = getColorCode(a.getDisplayName().getString());
        String cb = getColorCode(b.getDisplayName().getString());
        return !ca.isEmpty() && ca.equals(cb);
    }

    private String getColorCode(String name) {
        int idx = name.indexOf('\u00A7');
        if (idx >= 0 && idx + 1 < name.length()) return String.valueOf(name.charAt(idx + 1));
        return "";
    }

    private void attackEntity(MinecraftClient mc, LivingEntity target) {
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private float[] lookAt(Vec3d from, Vec3d to) {
        Vec3d dir = to.subtract(from).normalize();
        float yaw = (float) Math.toDegrees(Math.atan2(-dir.x, dir.z));
        float pitch = (float) Math.toDegrees(-Math.asin(dir.y));
        return new float[]{ yaw, pitch };
    }

    public LivingEntity getLockedTarget() { return lockedTarget; }
    public void toggleFriend(String name) { if (!friends.remove(name)) friends.add(name); }
    public int getKills() { return kills; }
    public int getHits() { return hits; }
    public int getCombo() { return combo; }
}
