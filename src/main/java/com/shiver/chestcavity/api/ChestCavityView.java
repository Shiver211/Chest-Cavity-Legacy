package com.shiver.chestcavity.api;

import com.shiver.chestcavity.capability.ChestCavityData;
import com.shiver.chestcavity.capability.ChestCavityMutations;
import com.shiver.chestcavity.runtime.ChestCavityRuntime;
import com.shiver.chestcavity.chest.organs.OrganData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.Map;

public final class ChestCavityView {

    private final ChestCavityData chestCavity;

    ChestCavityView(ChestCavityData chestCavity) {
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
        return chestCavity.getRuntime().getScoreValue(scoreId);
    }

    public Map<String, Float> getOrganScores() {
        return chestCavity.getRuntime().getScoreValues();
    }

    public boolean hasOrgan(ResourceLocation itemId) {
        return getOrganCount(itemId) > 0;
    }

    public int getOrganCount(ResourceLocation itemId) {
        return chestCavity.getRuntime().getOrganCount(itemId);
    }

    public int[] getOrganSlots(ResourceLocation itemId) {
        return chestCavity.getRuntime().getSlotsByItem(itemId);
    }

    public int[] getOrganSlotsByScore(String scoreId) {
        return chestCavity.getRuntime().getSlotsByScore(scoreId);
    }

    public ItemStack getOrgan(int slot) {
        return chestCavity.getRuntime().getOrgan(slot);
    }

    public OrganData getOrganData(int slot) {
        return chestCavity.getRuntime().getOrganData(slot);
    }

    public int[] getOccupiedSlots() {
        return chestCavity.getRuntime().getOccupiedSlots();
    }

    public ChestCavityRuntime getRuntime() {
        return chestCavity.getRuntime();
    }

    public void setOrgan(int slot, ItemStack stack) {
        ChestCavityMutations.setOrgan(chestCavity, slot, stack == null ? ItemStack.EMPTY : stack.copy());
    }

    public void recalculateScores() {
        ChestCavityMutations.recalculate(chestCavity);
    }

    public void openChestCavity() {
        chestCavity.openChestCavity();
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

    public ChestCavityData getInternal() {
        return chestCavity;
    }

}
