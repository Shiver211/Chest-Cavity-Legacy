package com.shiver.chestcavity.integration.crafttweaker.runtime;

import com.shiver.chestcavity.chest.ChestCavityInventory;
import com.shiver.chestcavity.chest.types.GeneratedChestCavityType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ChestCavityTypeDefinition extends GeneratedChestCavityType {

    public static final int MIN_SLOT = 0;
    public static final int MAX_SLOT = ChestCavityInventory.DEFAULT_SIZE - 1;

    private final ResourceLocation id;
    private final List<ExceptionalOrgan> exceptionalOrgans = new ArrayList<ExceptionalOrgan>();

    public ChestCavityTypeDefinition(ResourceLocation id) {
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }
        this.id = id;
    }

    public ResourceLocation getId() {
        return id;
    }

    public void setOrgan(int slot, ItemStack stack) {
        validateSlot(slot);
        getDefaultChestCavity().setStack(slot, copyStack(stack, stack == null ? 0 : stack.getCount()));
        clearDerivedCache();
    }

    public void setOrgan(int slot, ItemStack stack, int count) {
        validateSlot(slot);
        if (count <= 0) {
            throw new IllegalArgumentException("count must be greater than 0");
        }
        getDefaultChestCavity().setStack(slot, copyStack(stack, count));
        clearDerivedCache();
    }

    public void clearOrgan(int slot) {
        validateSlot(slot);
        getDefaultChestCavity().setStack(slot, ItemStack.EMPTY);
        clearDerivedCache();
    }

    public ItemStack getOrgan(int slot) {
        validateSlot(slot);
        ItemStack stack = getDefaultChestCavity().getStack(slot);
        return stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
    }

    public Map<Integer, ItemStack> getOrganLayout() {
        Map<Integer, ItemStack> layout = new LinkedHashMap<Integer, ItemStack>();
        ChestCavityInventory inventory = getDefaultChestCavity();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty()) {
                layout.put(i, stack.copy());
            }
        }
        return Collections.unmodifiableMap(layout);
    }

    public void addBaseScore(ResourceLocation scoreId, float value) {
        if (scoreId == null) {
            throw new IllegalArgumentException("scoreId cannot be null");
        }
        Map<ResourceLocation, Float> scores = new LinkedHashMap<ResourceLocation, Float>(getBaseOrganScores());
        scores.put(scoreId, value);
        setBaseOrganScores(scores);
    }

    public void forbidSlot(int slot) {
        validateSlot(slot);
        List<Integer> slots = new ArrayList<Integer>(getForbiddenSlots());
        if (!slots.contains(slot)) {
            slots.add(slot);
            setForbiddenSlots(slots);
        }
    }

    public void addExceptionalOrgan(IngredientDefinition ingredient, ResourceLocation scoreId, float value) {
        if (scoreId == null) {
            throw new IllegalArgumentException("scoreId cannot be null");
        }
        Map<ResourceLocation, Float> scores = new LinkedHashMap<ResourceLocation, Float>();
        scores.put(scoreId, value);
        addExceptionalOrgan(ingredient, scores);
    }

    public void addExceptionalOrgan(IngredientDefinition ingredient, Map<ResourceLocation, Float> scores) {
        if (ingredient == null) {
            throw new IllegalArgumentException("ingredient cannot be null");
        }
        exceptionalOrgans.add(ingredient.toExceptionalOrgan(scores));
        setExceptionalOrgans(exceptionalOrgans);
    }

    public List<ExceptionalOrgan> getExceptionalOrgansView() {
        return Collections.unmodifiableList(exceptionalOrgans);
    }

    private ItemStack copyStack(ItemStack stack, int count) {
        if (stack == null || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack copy = stack.copy();
        if (count <= 0) {
            throw new IllegalArgumentException("count must be greater than 0");
        }
        if (count > copy.getMaxStackSize()) {
            throw new IllegalArgumentException("count exceeds max stack size for " + copy.getItem().getRegistryName());
        }
        copy.setCount(count);
        return copy;
    }

    private void validateSlot(int slot) {
        if (slot < MIN_SLOT || slot > MAX_SLOT) {
            throw new IllegalArgumentException("slot must be between 0 and 26");
        }
    }
}
