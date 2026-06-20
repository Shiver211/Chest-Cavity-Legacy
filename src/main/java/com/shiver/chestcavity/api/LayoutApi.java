package com.shiver.chestcavity.api;

import com.shiver.chestcavity.content.ContentRegistry;
import com.shiver.chestcavity.layout.ChestLayoutDef;
import com.shiver.chestcavity.layout.LayoutMigrationStrategy;
import com.shiver.chestcavity.layout.SlotRule;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public final class LayoutApi {

    LayoutApi() {
    }

    public void registerGridLayout(ResourceLocation id, int slotCount, int slotsPerRow,
                                   int panelWidth, int panelHeight, int titleX, int titleY,
                                   int firstSlotX, int firstSlotY, int slotSpacingX, int slotSpacingY) {
        registerGridLayout(id, slotCount, slotsPerRow, panelWidth, panelHeight, titleX, titleY,
                firstSlotX, firstSlotY, slotSpacingX, slotSpacingY, LayoutMigrationStrategy.KEEP_BY_INDEX);
    }

    public void registerGridLayout(ResourceLocation id, int slotCount, int slotsPerRow,
                                   int panelWidth, int panelHeight, int titleX, int titleY,
                                   int firstSlotX, int firstSlotY, int slotSpacingX, int slotSpacingY,
                                   LayoutMigrationStrategy migrationStrategy) {
        registerGridLayout(id, slotCount, slotsPerRow, panelWidth, panelHeight, titleX, titleY,
                firstSlotX, firstSlotY, slotSpacingX, slotSpacingY, migrationStrategy, Collections.emptySet());
    }

    public void registerGridLayout(ResourceLocation id, int slotCount, int slotsPerRow,
                                   int panelWidth, int panelHeight, int titleX, int titleY,
                                   int firstSlotX, int firstSlotY, int slotSpacingX, int slotSpacingY,
                                   LayoutMigrationStrategy migrationStrategy, Set<Integer> forbiddenSlots) {
        ChestLayoutDef layout = new ChestLayoutDef(id, slotCount, slotsPerRow, panelWidth, panelHeight,
                titleX, titleY, firstSlotX, firstSlotY, slotSpacingX, slotSpacingY, migrationStrategy, forbiddenSlots);
        ContentRegistry.registerScriptLayout(layout);
    }

    public void setForbiddenSlots(ResourceLocation id, Iterable<Integer> forbiddenSlots) {
        ChestLayoutDef layout = ContentRegistry.getCompiled().getLayout(id);
        Set<Integer> slots = new LinkedHashSet<>();
        if (forbiddenSlots != null) {
            for (Integer slot : forbiddenSlots) {
                if (slot != null) {
                    slots.add(slot);
                }
            }
        }
        ChestLayoutDef updated = layout.withForbiddenSlots(slots);
        ContentRegistry.registerScriptLayout(updated);
    }

    public void setSlotRule(ResourceLocation id, int slot, boolean forbidden,
                            Iterable<ResourceLocation> allowedItems, Iterable<String> allowedScores,
                            int minStackSize, int maxStackSize) {
        ChestLayoutDef layout = ContentRegistry.getCompiled().getLayout(id);
        Set<ResourceLocation> items = new LinkedHashSet<>();
        if (allowedItems != null) {
            for (ResourceLocation item : allowedItems) {
                if (item != null) {
                    items.add(item);
                }
            }
        }
        Set<String> scores = new LinkedHashSet<>();
        if (allowedScores != null) {
            for (String score : allowedScores) {
                if (score != null) {
                    scores.add(score);
                }
            }
        }
        ContentRegistry.registerScriptLayout(layout.withSlotRule(slot,
                new SlotRule(forbidden, items, scores, minStackSize, maxStackSize)));
    }

    public ChestLayoutDef get(ResourceLocation id) {
        return ContentRegistry.getCompiled().getLayout(id);
    }
}
