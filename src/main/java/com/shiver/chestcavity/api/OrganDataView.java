package com.shiver.chestcavity.api;

import com.shiver.chestcavity.chest.organs.OrganData;

import java.util.Map;

public final class OrganDataView {

    private final OrganData data;

    OrganDataView(OrganData data) {
        this.data = data;
    }

    public boolean isPseudoOrgan() {
        return data.isPseudoOrgan();
    }

    public Map<String, Float> getOrganScores() {
        return data.getOrganScoresView();
    }
}
