package com.shiver.chestcavity.crt;

import com.shiver.chestcavity.api.ChestCavityApis;
import com.shiver.chestcavity.layout.LayoutMigrationStrategy;
import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.ArrayList;
import java.util.List;

@ZenRegister
@ZenClass("mods.chestcavity.ChestLayout")
public final class CrTChestLayout {

    private CrTChestLayout() {
    }

    @ZenMethod
    public static void registerGrid(String layoutId, int slotCount, int slotsPerRow,
                                    int panelWidth, int panelHeight, int titleX, int titleY,
                                    int firstSlotX, int firstSlotY, int slotSpacingX, int slotSpacingY) {
        ChestCavityApis.LAYOUTS.registerGridLayout(CrTUtil.id(layoutId), slotCount, slotsPerRow,
                panelWidth, panelHeight, titleX, titleY, firstSlotX, firstSlotY, slotSpacingX, slotSpacingY);
    }

    @ZenMethod
    public static void registerGrid(String layoutId, int slotCount, int slotsPerRow,
                                    int panelWidth, int panelHeight, int titleX, int titleY,
                                    int firstSlotX, int firstSlotY, int slotSpacingX, int slotSpacingY,
                                    String migrationStrategy) {
        ChestCavityApis.LAYOUTS.registerGridLayout(CrTUtil.id(layoutId), slotCount, slotsPerRow,
                panelWidth, panelHeight, titleX, titleY, firstSlotX, firstSlotY, slotSpacingX, slotSpacingY,
                LayoutMigrationStrategy.byName(migrationStrategy));
    }

    @ZenMethod
    public static void setSlotRule(String layoutId, int slot, boolean forbidden,
                                   String[] allowedItems, String[] allowedScores,
                                   int minStackSize, int maxStackSize) {
        ChestCavityApis.LAYOUTS.setSlotRule(CrTUtil.id(layoutId), slot, forbidden,
                ids(allowedItems), strings(allowedScores), minStackSize, maxStackSize);
    }

    private static List<net.minecraft.util.ResourceLocation> ids(String[] values) {
        List<net.minecraft.util.ResourceLocation> ids = new ArrayList<>();
        if (values != null) {
            for (String value : values) {
                ids.add(CrTUtil.id(value));
            }
        }
        return ids;
    }

    private static List<String> strings(String[] values) {
        List<String> strings = new ArrayList<>();
        if (values != null) {
            for (String value : values) {
                strings.add(value);
            }
        }
        return strings;
    }
}
