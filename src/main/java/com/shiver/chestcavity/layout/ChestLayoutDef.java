package com.shiver.chestcavity.layout;

import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class ChestLayoutDef {

    private final ResourceLocation id;
    private final int slotCount;
    private final int slotsPerRow;
    private final int panelWidth;
    private final int panelHeight;
    private final int titleX;
    private final int titleY;
    private final int firstSlotX;
    private final int firstSlotY;
    private final int slotSpacingX;
    private final int slotSpacingY;
    private final LayoutMigrationStrategy migrationStrategy;
    private final Map<Integer, SlotRule> slotRules;

    public ChestLayoutDef(ResourceLocation id, int slotCount, int slotsPerRow, int panelWidth, int panelHeight,
                          int titleX, int titleY, int firstSlotX, int firstSlotY,
                          int slotSpacingX, int slotSpacingY) {
        this(id, slotCount, slotsPerRow, panelWidth, panelHeight, titleX, titleY, firstSlotX, firstSlotY,
                slotSpacingX, slotSpacingY, LayoutMigrationStrategy.KEEP_BY_INDEX, Collections.emptySet());
    }

    public ChestLayoutDef(ResourceLocation id, int slotCount, int slotsPerRow, int panelWidth, int panelHeight,
                          int titleX, int titleY, int firstSlotX, int firstSlotY,
                          int slotSpacingX, int slotSpacingY, LayoutMigrationStrategy migrationStrategy) {
        this(id, slotCount, slotsPerRow, panelWidth, panelHeight, titleX, titleY, firstSlotX, firstSlotY,
                slotSpacingX, slotSpacingY, migrationStrategy, Collections.emptySet());
    }

    public ChestLayoutDef(ResourceLocation id, int slotCount, int slotsPerRow, int panelWidth, int panelHeight,
                          int titleX, int titleY, int firstSlotX, int firstSlotY,
                          int slotSpacingX, int slotSpacingY, LayoutMigrationStrategy migrationStrategy,
                          Set<Integer> forbiddenSlots) {
        this(id, slotCount, slotsPerRow, panelWidth, panelHeight, titleX, titleY, firstSlotX, firstSlotY,
                slotSpacingX, slotSpacingY, migrationStrategy, rulesFromForbiddenSlots(forbiddenSlots));
    }

    public ChestLayoutDef(ResourceLocation id, int slotCount, int slotsPerRow, int panelWidth, int panelHeight,
                          int titleX, int titleY, int firstSlotX, int firstSlotY,
                          int slotSpacingX, int slotSpacingY, LayoutMigrationStrategy migrationStrategy,
                          Map<Integer, SlotRule> slotRules) {
        if (id == null) {
            throw new IllegalArgumentException("Chest layout id must not be null");
        }
        if (slotCount < 0) {
            throw new IllegalArgumentException("Chest layout slot count must not be negative");
        }
        if (slotsPerRow <= 0) {
            throw new IllegalArgumentException("Chest layout slots per row must be positive");
        }
        this.id = id;
        this.slotCount = slotCount;
        this.slotsPerRow = slotsPerRow;
        this.panelWidth = panelWidth;
        this.panelHeight = panelHeight;
        this.titleX = titleX;
        this.titleY = titleY;
        this.firstSlotX = firstSlotX;
        this.firstSlotY = firstSlotY;
        this.slotSpacingX = slotSpacingX;
        this.slotSpacingY = slotSpacingY;
        this.migrationStrategy = migrationStrategy == null ? LayoutMigrationStrategy.KEEP_BY_INDEX : migrationStrategy;
        this.slotRules = sanitizeSlotRules(slotRules, slotCount);
    }

    public ResourceLocation getId() {
        return id;
    }

    public int getSlotCount() {
        return slotCount;
    }

    public int getSlotsPerRow() {
        return slotsPerRow;
    }

    public int getPanelWidth() {
        return panelWidth;
    }

    public int getPanelHeight() {
        return panelHeight;
    }

    public int getTitleX() {
        return titleX;
    }

    public int getTitleY() {
        return titleY;
    }

    public int getSlotX(int slot) {
        validateSlot(slot);
        return firstSlotX + (slot % slotsPerRow) * slotSpacingX;
    }

    public int getSlotY(int slot) {
        validateSlot(slot);
        return firstSlotY + (slot / slotsPerRow) * slotSpacingY;
    }

    public LayoutMigrationStrategy getMigrationStrategy() {
        return migrationStrategy;
    }

    public boolean isSlotForbidden(int slot) {
        validateSlot(slot);
        return getSlotRule(slot).isForbidden();
    }

    public Set<Integer> getForbiddenSlots() {
        Set<Integer> result = new LinkedHashSet<>();
        for (Map.Entry<Integer, SlotRule> entry : slotRules.entrySet()) {
            if (entry.getValue().isForbidden()) {
                result.add(entry.getKey());
            }
        }
        return Collections.unmodifiableSet(result);
    }

    public SlotRule getSlotRule(int slot) {
        validateSlot(slot);
        SlotRule rule = slotRules.get(slot);
        return rule == null ? SlotRule.OPEN : rule;
    }

    public Map<Integer, SlotRule> getSlotRules() {
        return slotRules;
    }

    public ChestLayoutDef withForbiddenSlots(Set<Integer> forbiddenSlots) {
        return new ChestLayoutDef(id, slotCount, slotsPerRow, panelWidth, panelHeight,
                titleX, titleY, firstSlotX, firstSlotY, slotSpacingX, slotSpacingY,
                migrationStrategy, rulesFromForbiddenSlots(forbiddenSlots));
    }

    public ChestLayoutDef withSlotRule(int slot, SlotRule rule) {
        validateSlot(slot);
        Map<Integer, SlotRule> rules = new LinkedHashMap<>(slotRules);
        if (rule == null || rule == SlotRule.OPEN) {
            rules.remove(slot);
        } else {
            rules.put(slot, rule);
        }
        return new ChestLayoutDef(id, slotCount, slotsPerRow, panelWidth, panelHeight,
                titleX, titleY, firstSlotX, firstSlotY, slotSpacingX, slotSpacingY,
                migrationStrategy, rules);
    }

    public ChestLayoutDef withSlotRules(Map<Integer, SlotRule> slotRules) {
        return new ChestLayoutDef(id, slotCount, slotsPerRow, panelWidth, panelHeight,
                titleX, titleY, firstSlotX, firstSlotY, slotSpacingX, slotSpacingY,
                migrationStrategy, slotRules);
    }

    private void validateSlot(int slot) {
        if (slot < 0 || slot >= slotCount) {
            throw new IndexOutOfBoundsException("Layout slot " + slot + " outside 0-" + (slotCount - 1));
        }
    }

    private static Map<Integer, SlotRule> sanitizeSlotRules(Map<Integer, SlotRule> rules, int slotCount) {
        if (rules == null || rules.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Integer, SlotRule> result = new LinkedHashMap<>();
        for (Map.Entry<Integer, SlotRule> entry : rules.entrySet()) {
            Integer slot = entry.getKey();
            SlotRule rule = entry.getValue();
            if (slot != null && slot >= 0 && slot < slotCount && rule != null) {
                result.put(slot, rule);
            }
        }
        return Collections.unmodifiableMap(result);
    }

    private static Map<Integer, SlotRule> rulesFromForbiddenSlots(Set<Integer> slots) {
        if (slots == null || slots.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Integer, SlotRule> rules = new LinkedHashMap<>();
        for (Integer slot : slots) {
            if (slot != null) {
                rules.put(slot, SlotRule.FORBIDDEN);
            }
        }
        return rules;
    }
}
