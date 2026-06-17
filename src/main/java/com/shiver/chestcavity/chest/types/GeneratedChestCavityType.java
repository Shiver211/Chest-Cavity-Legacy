package com.shiver.chestcavity.chest.types;

import com.shiver.chestcavity.chest.ChestCavityInventory;
import com.shiver.chestcavity.chest.organs.OrganData;
import com.shiver.chestcavity.chest.organs.OrganManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GeneratedChestCavityType implements ChestCavityType {

    private ChestCavityInventory defaultChestCavity = new ChestCavityInventory();
    private final Map<ResourceLocation, Float> baseOrganScores = new LinkedHashMap<>();
    private final List<Integer> forbiddenSlots = new ArrayList<>();
    private Map<ResourceLocation, Float> defaultOrganScores;
    private List<ItemStack> droppableOrgans;
    private float dropRateMultiplier = 1.0F;
    private boolean bossChestCavity;
    private boolean playerChestCavity;

    @Override
    public Map<ResourceLocation, Float> getDefaultOrganScores() {
        if (defaultOrganScores == null) {
            defaultOrganScores = new LinkedHashMap<>();
            loadBaseOrganScores(defaultOrganScores);
            addInventoryOrganScores(defaultOrganScores, defaultChestCavity);
        }
        return defaultOrganScores;
    }

    @Override
    public float getDefaultOrganScore(ResourceLocation id) {
        Float score = getDefaultOrganScores().get(id);
        return score == null ? 0.0F : score;
    }

    @Override
    public ChestCavityInventory getDefaultChestCavity() {
        return defaultChestCavity;
    }

    public void setDefaultChestCavity(ChestCavityInventory defaultChestCavity) {
        this.defaultChestCavity = defaultChestCavity == null ? new ChestCavityInventory() : defaultChestCavity;
        clearDerivedCache();
    }

    public Map<ResourceLocation, Float> getBaseOrganScores() {
        return Collections.unmodifiableMap(baseOrganScores);
    }

    public void setBaseOrganScores(Map<ResourceLocation, Float> scores) {
        baseOrganScores.clear();
        if (scores != null) {
            baseOrganScores.putAll(scores);
        }
        clearDerivedCache();
    }

    public List<Integer> getForbiddenSlots() {
        return Collections.unmodifiableList(forbiddenSlots);
    }

    public void setForbiddenSlots(List<Integer> slots) {
        forbiddenSlots.clear();
        if (slots != null) {
            forbiddenSlots.addAll(slots);
        }
        clearDerivedCache();
    }

    @Override
    public boolean isSlotForbidden(int index) {
        return forbiddenSlots.contains(index);
    }

    @Override
    public void fillChestCavityInventory(ChestCavityInventory chestCavity) {
        if (chestCavity == null) {
            return;
        }
        chestCavity.clear();
        for (int i = 0; i < chestCavity.size() && i < defaultChestCavity.size(); i++) {
            ItemStack stack = defaultChestCavity.getStack(i);
            chestCavity.setStack(i, stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
        }
    }

    @Override
    public void loadBaseOrganScores(Map<ResourceLocation, Float> organScores) {
        organScores.clear();
        organScores.putAll(baseOrganScores);
    }

    @Override
    public OrganData catchExceptionalOrgan(ItemStack stack) {
        return OrganManager.get(stack);
    }

    @Override
    public List<ItemStack> getDroppableOrgans() {
        if (droppableOrgans == null) {
            droppableOrgans = new LinkedList<>();
            for (int i = 0; i < defaultChestCavity.size(); i++) {
                ItemStack stack = defaultChestCavity.getStack(i);
                if (!stack.isEmpty()) {
                    droppableOrgans.add(stack.copy());
                }
            }
        }
        return Collections.unmodifiableList(droppableOrgans);
    }

    @Override
    public boolean isBossChestCavity() {
        return bossChestCavity;
    }

    public void setBossChestCavity(boolean bossChestCavity) {
        this.bossChestCavity = bossChestCavity;
    }

    @Override
    public boolean isPlayerChestCavity() {
        return playerChestCavity;
    }

    public void setPlayerChestCavity(boolean playerChestCavity) {
        this.playerChestCavity = playerChestCavity;
    }

    @Override
    public float getDropRateMultiplier() {
        return dropRateMultiplier;
    }

    public void setDropRateMultiplier(float dropRateMultiplier) {
        this.dropRateMultiplier = dropRateMultiplier;
    }

    public void clearDerivedCache() {
        defaultOrganScores = null;
        droppableOrgans = null;
    }

    private void addInventoryOrganScores(Map<ResourceLocation, Float> scores, ChestCavityInventory inventory) {
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) {
                continue;
            }

            OrganData data = catchExceptionalOrgan(stack);
            if (data == null) {
                continue;
            }

            float stackRatio = Math.min((float) stack.getCount() / (float) stack.getMaxStackSize(), 1.0F);
            for (Map.Entry<ResourceLocation, Float> entry : data.getOrganScores().entrySet()) {
                Float old = scores.get(entry.getKey());
                float value = entry.getValue() * stackRatio;
                scores.put(entry.getKey(), old == null ? value : old + value);
            }
        }
    }
}
