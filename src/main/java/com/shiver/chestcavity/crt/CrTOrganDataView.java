package com.shiver.chestcavity.crt;

import com.shiver.chestcavity.api.OrganDataView;
import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;

import java.util.Map;

@ZenRegister
@ZenClass("mods.chestcavity.OrganDataView")
public final class CrTOrganDataView {

    private final OrganDataView view;

    CrTOrganDataView(OrganDataView view) {
        this.view = view;
    }

    @ZenGetter("isPseudoOrgan")
    public boolean isPseudoOrgan() {
        return view.isPseudoOrgan();
    }

    @ZenGetter("organScores")
    public Map<String, Float> getOrganScores() {
        return view.getOrganScores();
    }
}
