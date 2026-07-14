package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

/**
 * AntiBot — определяет фейковых игроков (NPC, citizens, голых ботов).
 * Используется KillAura если включён filterBots.
 */
public class AntiBot extends Module {

    @Setting public boolean tabListCheck = true;     // боты обычно отсутствуют в табе
    @Setting public boolean nakedCheck   = true;     // голые игроки = боты
    @Setting public boolean stillCheck   = true;     // стоят на месте
    @Setting public boolean noHealthCheck = true;    // health = 0 при первом видении

    public AntiBot() { super("AntiBot", "Фильтр ботов и NPC"); }

    public boolean isBot(PlayerEntity p) {
        if (!isEnabled()) return false;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return false;

        if (tabListCheck && mc.getNetworkHandler() != null) {
            var entry = mc.getNetworkHandler().getPlayerListEntry(p.getUuid());
            if (entry == null) return true;
        }

        if (nakedCheck) {
            boolean armored = false;
            for (var stack : p.getArmorItems()) {
                if (!stack.isEmpty()) { armored = true; break; }
            }
            if (!armored && p.getMainHandStack().isEmpty()
                         && p.getOffHandStack().isEmpty()) {
                return true;
            }
        }

        if (stillCheck) {
            double dx = p.getX() - p.prevX;
            double dz = p.getZ() - p.prevZ;
            // Bots usually don't move at all
            if (Math.abs(dx) < 1e-4 && Math.abs(dz) < 1e-4 && p.age > 60) {
                return true;
            }
        }

        if (noHealthCheck && p.getHealth() <= 0f && p.age < 5) {
            return true;
        }

        return false;
    }
}
