package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.OneFizzMod;
import com.onefizz.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

/**
 * AutoClicker — пока зажата ЛКМ/ПКМ, автоматически генерирует клики
 * с человеко-подобной частотой (CPS).
 *
 * Источник состояния кнопок — mc.options.attackKey / useKey (а не GLFW),
 * чтобы корректно учитывались биндинги пользователя и пауза/GUI.
 *
 * Действие клика выполняется через interactionManager напрямую — это
 * обходит stock attackCooldown и заставляет атаку срабатывать каждый клик.
 */
public class AutoClicker extends Module {

    public enum ClickButton { LMB, RMB, BOTH }
    public enum Pattern { CONSTANT, JITTER, BURST, HUMAN }

    @Setting(name = "Кнопка клика")
    public ClickButton button = ClickButton.LMB;

    @Setting(name = "Паттерн кликов")
    public Pattern pattern = Pattern.HUMAN;

    @Setting(name = "Мин. кликов/сек", min = 1f, max = 25f)
    public float minCps = 8f;

    @Setting(name = "Макс. кликов/сек", min = 1f, max = 25f)
    public float maxCps = 14f;

    @Setting(name = "Ломать блоки")
    public boolean breakBlocks = true;

    @Setting(name = "Бить мобов/игроков")
    public boolean attackEntities = true;

    @Setting(name = "Юзать предметы (ПКМ)")
    public boolean useItems = true;

    @Setting(name = "Только с оружием")
    public boolean weaponOnly = false;

    @Setting(name = "Имитация пауз")
    public boolean takeBreaks = true;

    @Setting(name = "Сброс кулдауна атаки")
    public boolean resetCooldown = true;

    @Setting(name = "Авто-наводка")
    public boolean autoAim = false;

    @Setting(name = "Дальность наводки", min = 2f, max = 6f)
    public float aimRange = 4f;

    @Setting(name = "FOV наводки", min = 30f, max = 360f)
    public float aimFov = 90f;

    @Setting(name = "Скорость наводки", min = 0.1f, max = 1.0f)
    public float aimSpeed = 0.4f;

    @Setting(name = "Тихая наводка")
    public boolean silentAim = true;

    @Setting(name = "Игнор друзей")
    public boolean ignoreFriends = true;

    private long nextClickAtNs = 0;
    private long lastBreakAtMs = 0;
    private boolean inBreak = false;
    private long breakUntilMs = 0;
    private final Random random = new Random();

    // Burst state
    private int burstRemaining = 0;

    public AutoClicker() {
        super("AutoClicker", "Автокликер с настройкой CPS");
    }

    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null || mc.world == null) return;
        if (mc.interactionManager == null) return;
        // Не работаем когда открыто меню
        if (mc.currentScreen != null) return;

        long windowHandle = mc.getWindow().getHandle();
        boolean lmbHeld = GLFW.glfwGetMouseButton(windowHandle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean rmbHeld = GLFW.glfwGetMouseButton(windowHandle, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;

        boolean shouldClick = switch (button) {
            case LMB  -> lmbHeld;
            case RMB  -> rmbHeld;
            case BOTH -> lmbHeld || rmbHeld;
        };

        if (!shouldClick) {
            inBreak = false;
            burstRemaining = 0;
            return;
        }

        // Auto-aim: ищем ближайшего врага в FOV и наводимся
        if (autoAim) {
            var target = findAimTarget(mc);
            if (target != null) {
                aimAtTarget(mc, target);
            }
        }

        // weaponOnly: только если в основной руке оружие
        if (weaponOnly && !isWeapon(mc)) return;

        long nowNs = System.nanoTime();
        long nowMs = System.currentTimeMillis();

        // Take occasional micro-breaks (humanizer)
        if (takeBreaks) {
            if (inBreak) {
                if (nowMs >= breakUntilMs) inBreak = false;
                else return;
            } else if (nowMs - lastBreakAtMs > 4_000 + random.nextInt(8_000)) {
                inBreak = true;
                breakUntilMs = nowMs + 80 + random.nextInt(170);
                lastBreakAtMs = nowMs;
                return;
            }
        }

        if (nowNs < nextClickAtNs) return;

        // Сбрасываем cooldown атаки чтобы клики не глотались ванилкой
        if (resetCooldown) {
            try {
                ((com.onefizz.mixin.MinecraftClientAccessor)(Object) mc).setAttackCooldown(0);
            } catch (Throwable ignored) {}
        }

        if ((button == ClickButton.LMB || button == ClickButton.BOTH) && lmbHeld) {
            doLmb(mc);
        }
        if ((button == ClickButton.RMB || button == ClickButton.BOTH) && rmbHeld) {
            doRmb(mc);
        }

        nextClickAtNs = nowNs + computeDelayNs(nowNs);
    }

    // ── Click execution ───────────────────────────────────────────────────────

    private void doLmb(MinecraftClient mc) {
        HitResult hit = mc.crosshairTarget;

        if (hit != null && hit.getType() == HitResult.Type.ENTITY) {
            if (!attackEntities) {
                mc.player.swingHand(Hand.MAIN_HAND);
                return;
            }
            OneFizzMod.modules.getCriticals().prepareHit(mc);
            mc.interactionManager.attackEntity(mc.player,
                ((EntityHitResult) hit).getEntity());
            mc.player.swingHand(Hand.MAIN_HAND);
            return;
        }

        if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
            if (!breakBlocks) {
                mc.player.swingHand(Hand.MAIN_HAND);
                return;
            }
            BlockHitResult bhr = (BlockHitResult) hit;
            // attackBlock = "single click" на блок (начинает/продолжает разлом)
            // Это важнее чем updateBlockBreakingProgress — каждый вызов даёт
            // фреш-клик и стартует процесс заново если был отменён.
            mc.interactionManager.attackBlock(bhr.getBlockPos(), bhr.getSide());
            mc.player.swingHand(Hand.MAIN_HAND);
            return;
        }

        // Воздух — просто свинг анимации
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private void doRmb(MinecraftClient mc) {
        if (!useItems) return;
        HitResult hit = mc.crosshairTarget;

        // Защита от дублирования с FastPlace: если FastPlace активен и цель — блок,
        // ванилла сама ставит блоки быстро. AutoClicker не должен дублировать
        // (иначе ставится 2 слоя блоков за тик).
        boolean fastPlaceActive = OneFizzMod.modules.getFastPlace().isEnabled();
        if (fastPlaceActive && hit != null && hit.getType() == HitResult.Type.BLOCK) {
            return;
        }

        // Сначала — взаимодействие с сущностью
        if (hit != null && hit.getType() == HitResult.Type.ENTITY) {
            EntityHitResult ehr = (EntityHitResult) hit;
            ActionResult r = mc.interactionManager.interactEntityAtLocation(
                mc.player, ehr.getEntity(), ehr, Hand.MAIN_HAND);
            if (r.isAccepted()) {
                if (r.shouldSwingHand()) mc.player.swingHand(Hand.MAIN_HAND);
                return;
            }
            r = mc.interactionManager.interactEntity(mc.player, ehr.getEntity(), Hand.MAIN_HAND);
            if (r.isAccepted()) {
                if (r.shouldSwingHand()) mc.player.swingHand(Hand.MAIN_HAND);
                return;
            }
        }

        // Затем — клик по блоку
        if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult bhr = (BlockHitResult) hit;
            // Prevent placing block on top of the block directly under player's feet
            if (bhr.getSide() == net.minecraft.util.math.Direction.UP && mc.player.isOnGround()) {
                var blockPos = bhr.getBlockPos();
                var playerPos = mc.player.getBlockPos();
                if (blockPos.getX() == playerPos.getX()
                    && blockPos.getZ() == playerPos.getZ()
                    && blockPos.getY() == playerPos.getY() - 1) {
                    return;
                }
            }
            ActionResult r = mc.interactionManager.interactBlock(
                mc.player, Hand.MAIN_HAND, bhr);
            if (r.isAccepted()) {
                if (r.shouldSwingHand()) mc.player.swingHand(Hand.MAIN_HAND);
                return;
            }
        }

        // Иначе — использование предмета в воздухе (ест еду, стреляет луком и т.д.)
        ActionResult r = mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        if (r.isAccepted() && r.shouldSwingHand()) {
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }

    private boolean isWeapon(MinecraftClient mc) {
        var stack = mc.player.getMainHandStack();
        if (stack == null || stack.isEmpty()) return false;
        var item = stack.getItem();
        return item instanceof net.minecraft.item.SwordItem
            || item instanceof net.minecraft.item.AxeItem
            || item instanceof net.minecraft.item.TridentItem;
    }

    // ── Delay math ────────────────────────────────────────────────────────────

    private long computeDelayNs(long nowNs) {
        return switch (pattern) {
            case CONSTANT -> oneSecondsAt(midCps());
            case JITTER   -> oneSecondsAt(randomCpsInRange());
            case BURST    -> burstDelay();
            case HUMAN    -> humanDelay();
        };
    }

    private float midCps() { return (minCps + maxCps) / 2f; }

    private float randomCpsInRange() {
        return minCps + random.nextFloat() * (maxCps - minCps);
    }

    private long oneSecondsAt(float cps) {
        return (long)(1_000_000_000L / Math.max(0.5f, cps));
    }

    private long burstDelay() {
        if (burstRemaining > 0) {
            burstRemaining--;
            return oneSecondsAt(maxCps + 5f);
        }
        if (random.nextFloat() < 0.18f) {
            burstRemaining = 1 + random.nextInt(3);
            return oneSecondsAt(maxCps + 5f);
        }
        return oneSecondsAt(randomCpsInRange());
    }

    private long humanDelay() {
        float u = random.nextFloat();
        float v = random.nextFloat();
        float gauss = (float)(Math.sqrt(-2.0 * Math.log(Math.max(1e-7, u)))
                              * Math.cos(2 * Math.PI * v));
        float mid = midCps();
        float spread = (maxCps - minCps) / 4f;
        float cps = Math.max(0.5f, mid + gauss * spread);
        return oneSecondsAt(cps);
    }

    // ── Auto-aim ──────────────────────────────────────────────────────────────

    private net.minecraft.entity.LivingEntity findAimTarget(MinecraftClient mc) {
        var ka = OneFizzMod.modules.getKillAura();
        var antiBot = OneFizzMod.modules.getAntiBot();
        boolean useAntiBot = antiBot.isEnabled();

        return mc.world.getEntitiesByClass(
                net.minecraft.entity.LivingEntity.class,
                mc.player.getBoundingBox().expand(aimRange + 1.5),
                e -> {
                    if (e == mc.player || e.isDead()) return false;
                    if (mc.player.distanceTo(e) > aimRange) return false;

                    if (e instanceof net.minecraft.entity.player.PlayerEntity p) {
                        // Игнор друзей (общий список с KillAura)
                        if (ignoreFriends && ka.friends.contains(p.getName().getString())) return false;
                        if (useAntiBot && antiBot.isBot(p)) return false;
                    }

                    // FOV check
                    double angle = angleDelta(mc, e);
                    if (angle > aimFov / 2f) return false;
                    return true;
                })
            .stream()
            .min(java.util.Comparator.comparingDouble(e -> angleDelta(mc, e)))
            .orElse(null);
    }

    private void aimAtTarget(MinecraftClient mc, net.minecraft.entity.LivingEntity target) {
        var eyes = mc.player.getEyePos();
        var box = target.getBoundingBox();
        // Аим в ближайшую точку хитбокса
        var aimPoint = new net.minecraft.util.math.Vec3d(
            net.minecraft.util.math.MathHelper.clamp(eyes.x, box.minX, box.maxX),
            net.minecraft.util.math.MathHelper.clamp(eyes.y, box.minY, box.maxY),
            net.minecraft.util.math.MathHelper.clamp(eyes.z, box.minZ, box.maxZ));

        var dir = aimPoint.subtract(eyes).normalize();
        float targetYaw = (float) Math.toDegrees(Math.atan2(-dir.x, dir.z));
        float targetPitch = (float) Math.toDegrees(-Math.asin(dir.y));

        float curYaw, curPitch;
        if (silentAim && com.onefizz.RotationManager.isActive()) {
            curYaw = com.onefizz.RotationManager.getYaw();
            curPitch = com.onefizz.RotationManager.getPitch();
        } else {
            curYaw = mc.player.getYaw();
            curPitch = mc.player.getPitch();
        }

        // Smooth interpolation
        float diffYaw = net.minecraft.util.math.MathHelper.wrapDegrees(targetYaw - curYaw);
        float diffPitch = targetPitch - curPitch;
        float newYaw = curYaw + diffYaw * aimSpeed;
        float newPitch = net.minecraft.util.math.MathHelper.clamp(curPitch + diffPitch * aimSpeed, -90f, 90f);

        if (silentAim) {
            com.onefizz.RotationManager.setOverride(newYaw, newPitch);
        } else {
            mc.player.setYaw(net.minecraft.util.math.MathHelper.wrapDegrees(newYaw));
            mc.player.setPitch(newPitch);
        }
    }

    private double angleDelta(MinecraftClient mc, net.minecraft.entity.LivingEntity e) {
        var eyes = mc.player.getEyePos();
        var dir = e.getEyePos().subtract(eyes).normalize();
        float yaw = (float) Math.toDegrees(Math.atan2(-dir.x, dir.z));
        return Math.abs(net.minecraft.util.math.MathHelper.wrapDegrees(mc.player.getYaw() - yaw));
    }

    private String teamColor(net.minecraft.entity.player.PlayerEntity player) {
        String name = player.getDisplayName().getString();
        int idx = name.indexOf('\u00A7');
        if (idx >= 0 && idx + 1 < name.length()) return String.valueOf(name.charAt(idx + 1));
        return "";
    }
}
