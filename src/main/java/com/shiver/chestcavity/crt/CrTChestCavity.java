package com.shiver.chestcavity.crt;

import com.shiver.chestcavity.api.ChestCavityView;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.Map;

@ZenRegister
@ZenClass("mods.chestcavity.ChestCavityData")
public final class CrTChestCavity {

    private final ChestCavityView view;

    CrTChestCavity(ChestCavityView view) {
        this.view = view;
    }

    @ZenGetter("isOpened")
    public boolean isOpened() {
        return view.isOpened();
    }

    @ZenGetter("slotCount")
    public int getSlotCount() {
        return view.getSlotCount();
    }

    @ZenMethod
    public float getOrganScore(String scoreId) {
        return view.getOrganScore(scoreId);
    }

    @ZenMethod
    public Map<String, Float> getOrganScores() {
        return view.getOrganScores();
    }

    @ZenMethod
    public boolean hasOrgan(IItemStack item) {
        return view.hasOrgan(CrTUtil.itemId(item));
    }

    @ZenMethod
    public int getOrganCount(IItemStack item) {
        return view.getOrganCount(CrTUtil.itemId(item));
    }

    @ZenMethod
    public int[] getOrganSlots(IItemStack item) {
        return view.getOrganSlots(CrTUtil.itemId(item));
    }

    @ZenMethod
    public int[] getOrganSlotsByScore(String scoreId) {
        return view.getOrganSlotsByScore(scoreId);
    }

    @ZenMethod
    public int[] getOccupiedSlots() {
        return view.getOccupiedSlots();
    }

    @ZenMethod
    public boolean hasScore(String scoreId) {
        return view.getOrganScores().containsKey(scoreId);
    }

    @ZenMethod
    public IItemStack getOrgan(int slot) {
        return CrTUtil.stack(view.getOrgan(slot));
    }

    @ZenMethod
    public void setOrgan(int slot, IItemStack stack) {
        view.setOrgan(slot, CrTUtil.stack(stack));
    }

    @ZenMethod
    public void recalculateScores() {
        view.recalculateScores();
    }

    @ZenMethod
    public void openChestCavity() {
        view.openChestCavity();
    }
}
