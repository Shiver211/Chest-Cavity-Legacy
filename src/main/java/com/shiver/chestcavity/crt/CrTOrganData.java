package com.shiver.chestcavity.crt;

import com.shiver.chestcavity.api.ChestCavityApis;
import com.shiver.chestcavity.api.OrganDataView;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.Map;

@ZenRegister
@ZenClass("mods.chestcavity.OrganData")
public final class CrTOrganData {

    private CrTOrganData() {
    }

    @ZenMethod
    public static void register(IItemStack item, Map<String, Float> scores) {
        ChestCavityApis.ORGANS.register(CrTUtil.itemId(item), CrTUtil.ensureFloatMap(scores));
    }

    @ZenMethod
    public static void registerPseudo(IItemStack item, Map<String, Float> scores) {
        ChestCavityApis.ORGANS.registerPseudo(CrTUtil.itemId(item), CrTUtil.ensureFloatMap(scores));
    }

    @ZenMethod
    public static void addScore(IItemStack item, String scoreId, float value) {
        ChestCavityApis.ORGANS.addScore(CrTUtil.itemId(item), scoreId, value);
    }

    @ZenMethod
    public static void removeScore(IItemStack item, String scoreId) {
        ChestCavityApis.ORGANS.removeScore(CrTUtil.itemId(item), scoreId);
    }

    @ZenMethod
    public static void remove(IItemStack item) {
        ChestCavityApis.ORGANS.remove(CrTUtil.itemId(item));
    }

    @ZenMethod
    public static void setPseudo(IItemStack item, boolean value) {
        ChestCavityApis.ORGANS.setPseudo(CrTUtil.itemId(item), value);
    }

    @ZenMethod
    public static CrTOrganDataView get(IItemStack item) {
        OrganDataView view = ChestCavityApis.ORGANS.get(CrTUtil.itemId(item));
        return view == null ? null : new CrTOrganDataView(view);
    }
}
