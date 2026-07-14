package com.onefizz.modules;

import com.onefizz.Module;
import com.onefizz.Setting;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;

public class XRay extends Module {

    public final Set<Block> visibleBlocks = new HashSet<>();

    @Setting public boolean showDiamond    = true;
    @Setting public boolean showEmerald    = true;
    @Setting public boolean showGold       = true;
    @Setting public boolean showIron       = true;
    @Setting public boolean showCoal       = false;
    @Setting public boolean showRedstone   = true;
    @Setting public boolean showLapis      = true;
    @Setting public boolean showCopper     = false;
    @Setting public boolean showAncientDebris = true;
    @Setting public boolean showWater      = false;
    @Setting public boolean showLava       = false;

    public XRay() { super("XRay", "Рентген руд и блоков"); }

    @Override
    protected void onEnable() {
        rebuildSet();
        MinecraftClient.getInstance().worldRenderer.reload();
    }

    @Override
    protected void onDisable() {
        visibleBlocks.clear();
        MinecraftClient.getInstance().worldRenderer.reload();
    }

    public void rebuildSet() {
        visibleBlocks.clear();
        addIf(showDiamond,      "diamond_ore", "deepslate_diamond_ore");
        addIf(showEmerald,      "emerald_ore", "deepslate_emerald_ore");
        addIf(showGold,         "gold_ore",    "deepslate_gold_ore", "nether_gold_ore");
        addIf(showIron,         "iron_ore",    "deepslate_iron_ore");
        addIf(showCoal,         "coal_ore",    "deepslate_coal_ore");
        addIf(showRedstone,     "redstone_ore","deepslate_redstone_ore");
        addIf(showLapis,        "lapis_ore",   "deepslate_lapis_ore");
        addIf(showCopper,       "copper_ore",  "deepslate_copper_ore");
        addIf(showAncientDebris,"ancient_debris");
        addIf(showWater,        "water");
        addIf(showLava,         "lava");
    }

    private void addIf(boolean flag, String... ids) {
        if (!flag) return;
        for (String id : ids) {
            Block b = Registries.BLOCK.get(Identifier.of(id));
            if (b != null) visibleBlocks.add(b);
        }
    }
}
