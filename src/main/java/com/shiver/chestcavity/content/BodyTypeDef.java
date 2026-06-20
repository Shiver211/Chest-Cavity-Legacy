package com.shiver.chestcavity.content;

import com.shiver.chestcavity.chest.ChestCavityInventory;
import com.shiver.chestcavity.layout.ChestLayouts;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class BodyTypeDef {

    private final String id;
    private ChestCavityInventory defaultChestCavity = new ChestCavityInventory();
    private final Map<String, Float> baseOrganScores = new LinkedHashMap<>();
    private final List<ExceptionalOrganDef> exceptionalOrgans = new ArrayList<>();
    private final List<Integer> forbiddenSlots = new ArrayList<>();
    private float dropRateMultiplier = 1.0F;
    private boolean bossChestCavity;
    private boolean playerChestCavity;
    private ResourceLocation layoutId = ChestLayouts.DEFAULT_ID;

    public BodyTypeDef(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Body type id must not be empty");
        }
        this.id = id;
    }

    public BodyTypeDef(BodyTypeDef source) {
        this(source.id);
        setDefaultChestCavity(source.defaultChestCavity);
        setBaseOrganScores(source.baseOrganScores);
        setExceptionalOrgans(source.exceptionalOrgans);
        setForbiddenSlots(source.forbiddenSlots);
        dropRateMultiplier = source.dropRateMultiplier;
        bossChestCavity = source.bossChestCavity;
        playerChestCavity = source.playerChestCavity;
        layoutId = source.layoutId;
    }

    public String getId() {
        return id;
    }

    public ChestCavityInventory getDefaultChestCavity() {
        return copyInventory(defaultChestCavity);
    }

    public void setDefaultChestCavity(ChestCavityInventory inventory) {
        defaultChestCavity = copyInventory(inventory == null ? new ChestCavityInventory() : inventory);
    }

    public void setSlot(int slot, ItemStack stack) {
        if (slot >= 0 && slot < defaultChestCavity.size()) {
            defaultChestCavity.setStack(slot, stack == null ? ItemStack.EMPTY : stack.copy());
        }
    }

    public void clearSlots() {
        defaultChestCavity.clear();
    }

    public Map<String, Float> getBaseOrganScores() {
        return Collections.unmodifiableMap(baseOrganScores);
    }

    public void setBaseOrganScores(Map<String, Float> scores) {
        baseOrganScores.clear();
        if (scores != null) {
            baseOrganScores.putAll(scores);
        }
    }

    public void addBaseOrganScore(String scoreId, float value) {
        if (scoreId != null) {
            baseOrganScores.put(scoreId, value);
        }
    }

    public void removeBaseOrganScore(String scoreId) {
        if (scoreId != null) {
            baseOrganScores.remove(scoreId);
        }
    }

    public List<ExceptionalOrganDef> getExceptionalOrgans() {
        return Collections.unmodifiableList(exceptionalOrgans);
    }

    public void setExceptionalOrgans(List<ExceptionalOrganDef> organs) {
        exceptionalOrgans.clear();
        if (organs != null) {
            exceptionalOrgans.addAll(organs);
        }
    }

    public void addExceptionalOrgan(ExceptionalOrganDef organ) {
        if (organ != null) {
            exceptionalOrgans.add(organ);
        }
    }

    public void clearExceptionalOrgans() {
        exceptionalOrgans.clear();
    }

    public List<Integer> getForbiddenSlots() {
        return Collections.unmodifiableList(forbiddenSlots);
    }

    public void setForbiddenSlots(List<Integer> slots) {
        forbiddenSlots.clear();
        if (slots != null) {
            forbiddenSlots.addAll(slots);
        }
    }

    public void addForbiddenSlot(int slot) {
        if (!forbiddenSlots.contains(slot)) {
            forbiddenSlots.add(slot);
        }
    }

    public void removeForbiddenSlot(int slot) {
        forbiddenSlots.remove(Integer.valueOf(slot));
    }

    public float getDropRateMultiplier() {
        return dropRateMultiplier;
    }

    public void setDropRateMultiplier(float dropRateMultiplier) {
        this.dropRateMultiplier = dropRateMultiplier;
    }

    public boolean isBossChestCavity() {
        return bossChestCavity;
    }

    public void setBossChestCavity(boolean bossChestCavity) {
        this.bossChestCavity = bossChestCavity;
    }

    public boolean isPlayerChestCavity() {
        return playerChestCavity;
    }

    public void setPlayerChestCavity(boolean playerChestCavity) {
        this.playerChestCavity = playerChestCavity;
    }

    public ResourceLocation getLayoutId() {
        return layoutId;
    }

    public void setLayoutId(ResourceLocation layoutId) {
        this.layoutId = layoutId == null ? ChestLayouts.DEFAULT_ID : layoutId;
    }

    public BodyTypeDef copy() {
        return new BodyTypeDef(this);
    }

    private static ChestCavityInventory copyInventory(ChestCavityInventory source) {
        ChestCavityInventory copy = new ChestCavityInventory(source == null ? ChestLayouts.DEFAULT.getSlotCount() : source.size());
        if (source != null) {
            for (int i = 0; i < source.size() && i < copy.size(); i++) {
                ItemStack stack = source.getStack(i);
                copy.setStack(i, stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
            }
        }
        return copy;
    }
}
