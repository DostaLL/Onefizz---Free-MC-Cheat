package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.OneFizzMod;
import com.onefizz.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.SwordItem;

public class AutoWeapon extends Module {

    public enum Mode { ALWAYS, KILLAURA }

    @Setting(name = "Режим") public Mode mode = Mode.KILLAURA;
    @Setting(name = "Задержка (тики)", min = 0f, max = 10f) public int switchDelay = 1;

    private int cooldown = 0;

    public AutoWeapon() { super("AutoWeapon", "Автосмена оружия при атаке"); }

    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null) return;
        if (cooldown > 0) { cooldown--; return; }

        if (mode == Mode.KILLAURA && !OneFizzMod.modules.getKillAura().isEnabled()) return;
        if (!mc.options.attackKey.isPressed()) return;

        trySwitch(mc);
    }

    public void trySwitch(MinecraftClient mc) {
        if (mc.player == null || cooldown > 0) return;

        var inv = mc.player.getInventory();
        var current = inv.getMainHandStack();
        if (current.getItem() instanceof SwordItem) return;

        int bestSlot = -1;
        double bestDmg = 0;

        for (int i = 0; i < 9; i++) {
            var stack = inv.getStack(i);
            if (!(stack.getItem() instanceof SwordItem)) continue;

            AttributeModifiersComponent comp = stack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
            double dmg = 0;
            if (comp != null) {
                dmg = comp.modifiers().stream()
                    .filter(e -> e.attribute().equals(EntityAttributes.GENERIC_ATTACK_DAMAGE))
                    .mapToDouble(e -> e.modifier().value())
                    .sum();
            }

            if (dmg > bestDmg) {
                bestDmg = dmg;
                bestSlot = i;
            }
        }

        if (bestSlot != -1) {
            inv.selectedSlot = bestSlot;
            cooldown = switchDelay;
        }
    }
}
