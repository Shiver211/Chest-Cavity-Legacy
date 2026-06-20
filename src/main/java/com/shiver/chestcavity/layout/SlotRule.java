package com.shiver.chestcavity.layout;

import com.shiver.chestcavity.chest.organs.OrganData;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class SlotRule {

    public static final SlotRule OPEN = new SlotRule(false, Collections.emptySet(), Collections.emptySet(), 0, 64);
    public static final SlotRule FORBIDDEN = new SlotRule(true, Collections.emptySet(), Collections.emptySet(), 0, 0);

    private final boolean forbidden;
    private final Set<ResourceLocation> allowedItems;
    private final Set<String> allowedScores;
    private final int minStackSize;
    private final int maxStackSize;

    public SlotRule(boolean forbidden, Set<ResourceLocation> allowedItems, Set<String> allowedScores,
                    int minStackSize, int maxStackSize) {
        this.forbidden = forbidden;
        this.allowedItems = sanitizeItems(allowedItems);
        this.allowedScores = sanitizeScores(allowedScores);
        this.minStackSize = Math.max(0, minStackSize);
        this.maxStackSize = Math.max(0, maxStackSize <= 0 ? 64 : maxStackSize);
    }

    public boolean isForbidden() {
        return forbidden;
    }

    public Set<ResourceLocation> getAllowedItems() {
        return allowedItems;
    }

    public Set<String> getAllowedScores() {
        return allowedScores;
    }

    public int getMinStackSize() {
        return minStackSize;
    }

    public int getMaxStackSize() {
        return maxStackSize;
    }

    public boolean canPlace(ItemStack stack, OrganData data) {
        if (forbidden) {
            return stack == null || stack.isEmpty();
        }
        if (stack == null || stack.isEmpty()) {
            return true;
        }
        if (stack.getCount() < minStackSize || stack.getCount() > maxStackSize) {
            return false;
        }
        ResourceLocation itemId = stack.getItem() == null ? null : stack.getItem().getRegistryName();
        if (!allowedItems.isEmpty() && !allowedItems.contains(itemId)) {
            return false;
        }
        if (!allowedScores.isEmpty()) {
            if (data == null) {
                return false;
            }
            for (String scoreId : allowedScores) {
                if (data.getOrganScores().containsKey(scoreId)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    public SlotRule withForbidden(boolean forbidden) {
        return new SlotRule(forbidden, allowedItems, allowedScores, minStackSize, forbidden ? 0 : maxStackSize);
    }

    public SlotRule withAllowedItems(Set<ResourceLocation> items) {
        return new SlotRule(forbidden, items, allowedScores, minStackSize, maxStackSize);
    }

    public SlotRule withAllowedScores(Set<String> scores) {
        return new SlotRule(forbidden, allowedItems, scores, minStackSize, maxStackSize);
    }

    public SlotRule withStackSize(int minStackSize, int maxStackSize) {
        return new SlotRule(forbidden, allowedItems, allowedScores, minStackSize, maxStackSize);
    }

    public static SlotRule merge(SlotRule base, SlotRule override) {
        if (base == null) {
            return override == null ? OPEN : override;
        }
        if (override == null) {
            return base;
        }
        Set<ResourceLocation> items = override.allowedItems.isEmpty() ? base.allowedItems : override.allowedItems;
        Set<String> scores = override.allowedScores.isEmpty() ? base.allowedScores : override.allowedScores;
        int min = override.minStackSize == 0 ? base.minStackSize : override.minStackSize;
        int max = override.maxStackSize == 64 ? base.maxStackSize : override.maxStackSize;
        return new SlotRule(base.forbidden || override.forbidden, items, scores, min, max);
    }

    private static Set<ResourceLocation> sanitizeItems(Set<ResourceLocation> source) {
        if (source == null || source.isEmpty()) {
            return Collections.emptySet();
        }
        Set<ResourceLocation> result = new LinkedHashSet<>();
        for (ResourceLocation id : source) {
            if (id != null) {
                result.add(id);
            }
        }
        return Collections.unmodifiableSet(result);
    }

    private static Set<String> sanitizeScores(Set<String> source) {
        if (source == null || source.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> result = new LinkedHashSet<>();
        for (String id : source) {
            if (id != null && !id.isEmpty()) {
                result.add(id);
            }
        }
        return Collections.unmodifiableSet(result);
    }
}
