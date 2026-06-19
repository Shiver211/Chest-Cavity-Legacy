package com.shiver.chestcavity.crt;

import com.shiver.chestcavity.api.ChestCavityApis;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenClass("mods.chestcavity.DropManager")
public final class CrTDropManager {

    private CrTDropManager() {
    }

    @ZenMethod
    public static void addOrganDrop(String entityId, IItemStack stack, int weight) {
        ChestCavityApis.DROPS.addOrganDrop(CrTUtil.id(entityId), CrTUtil.stack(stack), weight);
    }

    @ZenMethod
    public static void setDropProbability(String entityId, float value) {
        ChestCavityApis.DROPS.setDropProbability(CrTUtil.id(entityId), value);
    }

    @ZenMethod
    public static void removeOrganDrop(String entityId, IItemStack stack) {
        ChestCavityApis.DROPS.removeOrganDrop(CrTUtil.id(entityId), CrTUtil.stack(stack));
    }

    @ZenMethod
    public static void removeAllOrganDrops(String entityId) {
        ChestCavityApis.DROPS.removeAllOrganDrops(CrTUtil.id(entityId));
    }
}
