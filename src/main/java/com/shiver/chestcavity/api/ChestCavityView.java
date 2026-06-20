package com.shiver.chestcavity.api;

import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.capability.IChestCavity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class ChestCavityView {

    private final IChestCavity chestCavity;

    ChestCavityView(IChestCavity chestCavity) {
        this.chestCavity = chestCavity;
    }

    public EntityLivingBase getOwner() {
        return chestCavity.getOwner();
    }

    public boolean isOpened() {
        return chestCavity.isOpened();
    }

    public int getSlotCount() {
        return chestCavity.getSlotCount();
    }

    public float getOrganScore(String scoreId) {
        return chestCavity.getOrganScore(scoreId);
    }

    public Map<String, Float> getOrganScores() {
        return Collections.unmodifiableMap(chestCavity.getOrganScores());
    }

    public boolean hasOrgan(ResourceLocation itemId) {
        return getOrganCount(itemId) > 0;
    }

    public int getOrganCount(ResourceLocation itemId) {
        int count = 0;
        for (ItemStack stack : chestCavity.getOrgans()) {
            if (matches(stack, itemId)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public int[] getOrganSlots(ResourceLocation itemId) {
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < chestCavity.getSlotCount(); i++) {
            if (matches(chestCavity.getOrgan(i), itemId)) {
                slots.add(i);
            }
        }
        int[] result = new int[slots.size()];
        for (int i = 0; i < slots.size(); i++) {
            result[i] = slots.get(i);
        }
        return result;
    }

    public ItemStack getOrgan(int slot) {
        ItemStack stack = chestCavity.getOrgan(slot);
        return stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
    }

    public void setOrgan(int slot, ItemStack stack) {
        ChestCavityHelper.setOrganAndRecalculate(chestCavity, slot, stack == null ? ItemStack.EMPTY : stack.copy());
    }

    public void setOrganScore(String scoreId, float value) {
        chestCavity.setOrganScore(scoreId, value);
        ChestCavityHelper.applyAndSyncScoreChanges(chestCavity);
    }

    public void addOrganScore(String scoreId, float value) {
        chestCavity.addOrganScore(scoreId, value);
        ChestCavityHelper.applyAndSyncScoreChanges(chestCavity);
    }

    public void recalculateScores() {
        ChestCavityHelper.recalculateOrganScores(chestCavity);
        ChestCavityHelper.applyAndSyncScoreChanges(chestCavity);
    }

    public void openChestCavity() {
        ChestCavityHelper.openChestCavity(chestCavity);
    }

    public int getHeartBleedTimer() {
        return chestCavity.getHeartBleedTimer();
    }

    public void setHeartBleedTimer(int value) {
        chestCavity.setHeartBleedTimer(value);
    }

    public int getBloodPoisonTimer() {
        return chestCavity.getBloodPoisonTimer();
    }

    public void setBloodPoisonTimer(int value) {
        chestCavity.setBloodPoisonTimer(value);
    }

    public int getLiverTimer() {
        return chestCavity.getLiverTimer();
    }

    public void setLiverTimer(int value) {
        chestCavity.setLiverTimer(value);
    }

    public int getFurnaceProgress() {
        return chestCavity.getFurnaceProgress();
    }

    public void setFurnaceProgress(int value) {
        chestCavity.setFurnaceProgress(value);
    }

    public int getPhotosynthesisProgress() {
        return chestCavity.getPhotosynthesisProgress();
    }

    public void setPhotosynthesisProgress(int value) {
        chestCavity.setPhotosynthesisProgress(value);
    }

    public int getConnectedCrystalId() {
        return chestCavity.getConnectedCrystalId();
    }

    public void setConnectedCrystalId(int value) {
        chestCavity.setConnectedCrystalId(value);
    }

    public IChestCavity getInternal() {
        return chestCavity;
    }

    private boolean matches(ItemStack stack, ResourceLocation itemId) {
        if (stack == null || stack.isEmpty() || itemId == null) {
            return false;
        }
        Item item = stack.getItem();
        return item != null && itemId.equals(item.getRegistryName());
    }
}
