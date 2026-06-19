package com.shiver.chestcavity.crt;

import com.shiver.chestcavity.api.ChestCavityView;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenSetter;

import java.util.Map;

@ZenRegister
@ZenClass("mods.chestcavity.IChestCavity")
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
    public boolean hasOrgan(String itemId) {
        return view.hasOrgan(CrTUtil.id(itemId));
    }

    @ZenMethod
    public int getOrganCount(String itemId) {
        return view.getOrganCount(CrTUtil.id(itemId));
    }

    @ZenMethod
    public int[] getOrganSlots(String itemId) {
        return view.getOrganSlots(CrTUtil.id(itemId));
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
    public void setOrganScore(String scoreId, float value) {
        view.setOrganScore(scoreId, value);
    }

    @ZenMethod
    public void addOrganScore(String scoreId, float value) {
        view.addOrganScore(scoreId, value);
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
