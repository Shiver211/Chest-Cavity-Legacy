package com.shiver.chestcavity.compat.crafttweaker;

import com.shiver.chestcavity.chest.organs.OrganData;
import net.minecraft.util.ResourceLocation;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenProperty;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractOrganRepresentation {

    @ZenProperty
    public boolean pseudoOrgan;

    protected final Map<ResourceLocation, Float> organScores = new LinkedHashMap<>();

    @ZenMethod
    public void addScore(String id, float value) {
        organScores.put(CtCompatUtil.requireId(id, "score"), value);
    }

    @ZenMethod
    public void bindAbility(String id, float value) {
        organScores.put(CtCompatUtil.requireId(id, "ability"), value);
    }

    protected OrganData buildOrganData() {
        OrganData data = new OrganData();
        data.setPseudoOrgan(pseudoOrgan);
        data.setOrganScores(organScores);
        return data;
    }

    protected Map<ResourceLocation, Float> getScoresView() {
        return new LinkedHashMap<>(organScores);
    }
}
