package com.shiver.chestcavity.crt;

import com.shiver.chestcavity.api.ChestCavityApis;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.Map;

@ZenRegister
@ZenClass("mods.chestcavity.ChestCavityType")
public final class CrTChestCavityType {

    private CrTChestCavityType() {
    }

    @ZenMethod
    public static void register(String typeId) {
        ChestCavityApis.TYPES.register(typeId);
    }

    @ZenMethod
    public static void addBaseScore(String typeId, String scoreId, float value) {
        ChestCavityApis.TYPES.addBaseScore(typeId, scoreId, value);
    }

    @ZenMethod
    public static void removeBaseScore(String typeId, String scoreId) {
        ChestCavityApis.TYPES.removeBaseScore(typeId, scoreId);
    }

    @ZenMethod
    public static void setSlot(String typeId, int index, IItemStack stack) {
        ChestCavityApis.TYPES.setSlot(typeId, index, CrTUtil.stack(stack));
    }

    @ZenMethod
    public static void clearSlots(String typeId) {
        ChestCavityApis.TYPES.clearSlots(typeId);
    }

    @ZenMethod
    public static void addForbiddenSlot(String typeId, int slot) {
        ChestCavityApis.TYPES.addForbiddenSlot(typeId, slot);
    }

    @ZenMethod
    public static void removeForbiddenSlot(String typeId, int slot) {
        ChestCavityApis.TYPES.removeForbiddenSlot(typeId, slot);
    }

    @ZenMethod
    public static void setDropRateMultiplier(String typeId, float value) {
        ChestCavityApis.TYPES.setDropRateMultiplier(typeId, value);
    }

    @ZenMethod
    public static void setBossChestCavity(String typeId, boolean value) {
        ChestCavityApis.TYPES.setBossChestCavity(typeId, value);
    }

    @ZenMethod
    public static void setPlayerChestCavity(String typeId, boolean value) {
        ChestCavityApis.TYPES.setPlayerChestCavity(typeId, value);
    }

    @ZenMethod
    public static void addExceptionalOrgan(String typeId, IItemStack item, Map<String, Float> scores) {
        ItemStack stack = CrTUtil.stack(item);
        ChestCavityApis.TYPES.addExceptionalOrgan(typeId, stack.isEmpty() ? null : stack.getItem(), CrTUtil.ensureFloatMap(scores));
    }

    @ZenMethod
    public static void addExceptionalOrganByOre(String typeId, String oreName, Map<String, Float> scores) {
        ChestCavityApis.TYPES.addExceptionalOrganByOre(typeId, oreName, CrTUtil.ensureFloatMap(scores));
    }

    @ZenMethod
    public static void clearExceptionalOrgans(String typeId) {
        ChestCavityApis.TYPES.clearExceptionalOrgans(typeId);
    }

    @ZenMethod
    public static boolean isBossChestCavity(String typeId) {
        return ChestCavityApis.TYPES.isBossChestCavity(typeId);
    }

    @ZenMethod
    public static boolean isPlayerChestCavity(String typeId) {
        return ChestCavityApis.TYPES.isPlayerChestCavity(typeId);
    }

    @ZenMethod
    public static float getDropRateMultiplier(String typeId) {
        return ChestCavityApis.TYPES.getDropRateMultiplier(typeId);
    }
}
