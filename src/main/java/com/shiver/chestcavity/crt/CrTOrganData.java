package com.shiver.chestcavity.crt;

import com.shiver.chestcavity.api.ChestCavityApis;
import com.shiver.chestcavity.api.OrganDataView;
import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.Map;

@ZenRegister
@ZenClass("mods.chestcavity.OrganData")
public final class CrTOrganData {

    private CrTOrganData() {
    }

    @ZenMethod
    public static void register(String itemId, Map<String, Float> scores) {
        ChestCavityApis.ORGANS.register(CrTUtil.id(itemId), CrTUtil.ensureFloatMap(scores));
    }

    @ZenMethod
    public static void registerPseudo(String itemId, Map<String, Float> scores) {
        ChestCavityApis.ORGANS.registerPseudo(CrTUtil.id(itemId), CrTUtil.ensureFloatMap(scores));
    }

    @ZenMethod
    public static void addScore(String itemId, String scoreId, float value) {
        ChestCavityApis.ORGANS.addScore(CrTUtil.id(itemId), scoreId, value);
    }

    @ZenMethod
    public static void removeScore(String itemId, String scoreId) {
        ChestCavityApis.ORGANS.removeScore(CrTUtil.id(itemId), scoreId);
    }

    @ZenMethod
    public static void remove(String itemId) {
        ChestCavityApis.ORGANS.remove(CrTUtil.id(itemId));
    }

    @ZenMethod
    public static void setPseudo(String itemId, boolean value) {
        ChestCavityApis.ORGANS.setPseudo(CrTUtil.id(itemId), value);
    }

    @ZenMethod
    public static CrTOrganDataView get(String itemId) {
        OrganDataView view = ChestCavityApis.ORGANS.get(CrTUtil.id(itemId));
        return view == null ? null : new CrTOrganDataView(view);
    }
}
