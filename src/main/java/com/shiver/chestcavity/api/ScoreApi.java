package com.shiver.chestcavity.api;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ScoreApi {

    private final Map<String, String> displayNames = new LinkedHashMap<String, String>();

    ScoreApi() {
    }

    public void addScore(String scoreId, String displayName) {
        if (scoreId != null && displayName != null) {
            displayNames.put(scoreId, displayName);
        }
    }

    public String getDisplayName(String scoreId) {
        return scoreId == null ? null : displayNames.get(scoreId);
    }

    public Map<String, String> getDisplayNames() {
        return Collections.unmodifiableMap(displayNames);
    }
}
